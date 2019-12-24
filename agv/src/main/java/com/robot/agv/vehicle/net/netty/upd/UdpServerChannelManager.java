package com.robot.agv.vehicle.net.netty.upd;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.ScheduledFuture;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkState;

/**
 * Created by laotang on 2019/12/21.
 *
 *  @param <O> The type of outgoing messages on this UdpServerChannelManager.
 * @param <I> The type of incoming messages on this UdpServerChannelManager.
 */
public class UdpServerChannelManager<O, I> {

    private static final Logger LOG = LoggerFactory.getLogger(UdpServerChannelManager.class);

    private Bootstrap bootstrap;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;
    private ScheduledFuture<?> connectFuture;
    private ConnectionEventListener<I> connectionEventListener;
    private Supplier<List<ChannelHandler>> channelSupplier;
    private String host;
    private int port;
    private int readTimeout;
    private boolean enableLogging;

    private boolean initialized;
    private static int BUFFER_SIZE = 64 * 1024;
    private InetSocketAddress address;
    private static final String LOGGING_HANDLER_NAME = "ChannelLoggingHandler";

    public UdpServerChannelManager(@Nonnull ConnectionEventListener<I> connEventListener,
                                   Supplier<List<ChannelHandler>> channelSupplier,
                                   int readTimeout,
                                   boolean enableLogging) {
        this.connectionEventListener = connEventListener;
        this.channelSupplier = channelSupplier;
        this.readTimeout = readTimeout;
        this.enableLogging = enableLogging;
    }

    public void initialized() {
        if (this.initialized) {
            LOG.warn("已经初始化，请勿重复初始化");
            return;
        }
        this.bootstrap = new Bootstrap();
        workerGroup = new NioEventLoopGroup();
        this.bootstrap.group(workerGroup);
        this.bootstrap.channel(NioDatagramChannel.class);
        // 支持广播
        this.bootstrap.option(ChannelOption.SO_BROADCAST, true);
        // 设置UDP读缓冲区为64k
        this.bootstrap.option(ChannelOption.SO_RCVBUF, BUFFER_SIZE);
        // 设置UDP写缓冲区为64k
        this.bootstrap.option(ChannelOption.SO_SNDBUF, BUFFER_SIZE);
        UdpHandler udpHandler = new UdpHandler(this, connectionEventListener);
        this.bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel ch) throws Exception {
                Iterator channelIterator = ((List) UdpServerChannelManager.this.channelSupplier.get()).iterator();
                while (channelIterator.hasNext()) {
                    ChannelHandler handler = (ChannelHandler) channelIterator.next();
                    ch.pipeline().addLast(new ChannelHandler[]{handler});
                }
                ch.pipeline().addLast(udpHandler);
            }
        });
    }

    public void setSendAddress(InetSocketAddress address) {
        this.address = address;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void terminate() {
        disconnect();
    }

    public void connect(String host, int port) {
        requireNonNull(host, "host");
        checkState(isInitialized(), "Not initialized");
        if (isConnected()) {
            LOG.debug("Already connected, doing nothing.");
            return;
        }
        LOG.warn("Initiating connection attempt to {}:{}...", host, port);
        channelFuture = bootstrap.connect(host, port);
        channelFuture.addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                this.initialized = true;
                connectionEventListener.onConnect();
                this.host = host;
                this.port = port;
                LOG.warn("UdpServerChannelManager connect is success:  {}:{}", host, port);
            }
            else {
                connectionEventListener.onFailedConnectionAttempt();
                LOG.error("UdpServerChannelManager connect fail");
            }
        });
        connectFuture = null;
    }

    public void disconnect() {
        if (!this.initialized) {
            LOG.warn("UdpServerChannelManager is not initalized!");
            return;
        }
        this.channelFuture.channel().close();
        this.bootstrap.config().group().shutdownGracefully();
        this.initialized = false;
        this.channelFuture = null;
        LOG.warn("UdpServerChannelManager is disconnect!");
    }

    public boolean isConnected() {
        return channelFuture != null && channelFuture.channel().isActive();
    }

    public void setLoggingEnabled(boolean enabled) {
        checkState(initialized, "Not initialized.");

        if (null == channelFuture) {
            LOG.warn("No channel future available, doing nothing.");
            return;
        }

        ChannelPipeline pipeline = channelFuture.channel().pipeline();
        if (enabled && pipeline.get(LOGGING_HANDLER_NAME) == null) {
            pipeline.addFirst(LOGGING_HANDLER_NAME,
                    new LoggingHandler(UdpServerChannelManager.this.getClass()));
        }
        else if (!enabled && pipeline.get(LOGGING_HANDLER_NAME) != null) {
            pipeline.remove(LOGGING_HANDLER_NAME);
        }
    }

    public void scheduleConnect(@Nonnull String host, int port, long delay) {
        requireNonNull(host, "host");
        checkState(isInitialized(), "Not initialized");
        checkState(channelFuture == null, "Connection attempt already scheduled");

        connectFuture = workerGroup.schedule(() -> connect(host, port), delay, TimeUnit.MILLISECONDS);
    }

    public void send(O telegram) {
        if (!this.isConnected()) {
            throw new IllegalArgumentException("Not initialized.");
        }
        if (ObjectUtil.isEmpty(telegram)) {
            throw new IllegalArgumentException("广播的报文内容不能为空");
        }

        if (null == address && StrUtil.isNotEmpty(host) && port >0) {
            address = new InetSocketAddress(host, port);
        }

        LOG.info("upd server send client {} ", (address.getAddress().toString()+":"+address.getPort()));
        String telegramStr = telegram.toString();
        channelFuture.channel().writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(telegramStr, CharsetUtil.UTF_8),  address));
        address = null;
    }


}
