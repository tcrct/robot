package com.robot.agv.vehicle.net.netty.upd;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;


public class UdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final static Log logger = LogFactory.get();
    private ConnectionEventListener eventListener;
    private UdpServerChannelManager manager;


    public UdpHandler(UdpServerChannelManager manager, ConnectionEventListener eventListener){
        this.manager = manager;
        this.eventListener = eventListener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
        try {
            // 因为Netty对UDP进行了封装，所以接收到的是DatagramPacket对象。
            String telegram = datagramPacket.content().toString(CharsetUtil.UTF_8);
            if(StrUtil.isEmpty(telegram)) {
                logger.error("upd接收到的报文内容不能为空");
                return;
            }
            if(ObjectUtil.isNotEmpty(eventListener)) {
                // 接收到的报文，以字符串形式传递
                eventListener.onIncomingTelegram(telegram);
                // 将地址回调
                manager.setSendAddress(datagramPacket.sender());
            }
//            ctx.channel().writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(response.toString(), CharsetUtil.UTF_8), datagramPacket.sender()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)throws Exception {
        ctx.close();
        logger.error("UdpHandler exception: " + cause.getMessage(), cause);
    }
}
