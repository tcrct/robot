package com.robot.agv.vehicle.net.tcp;

import com.google.common.primitives.Ints;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.RobotCommAdapter;
import com.robot.agv.vehicle.comm.VehicleTelegramDecoder;
import com.robot.agv.vehicle.comm.VehicleTelegramEncoder;
import com.robot.agv.vehicle.net.IChannelManager;
import com.robot.agv.vehicle.telegrams.OrderResponse;
import com.robot.agv.vehicle.telegrams.StateResponse;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.opentcs.contrib.tcp.netty.TcpClientChannelManager;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/***
 * TCP方式
 */
public class TcpChannelManager implements IChannelManager<Request, Response> {

    private TcpClientChannelManager tcpClientChannelManager;
    private RobotCommAdapter robotCommAdapter;

    public TcpChannelManager(RobotCommAdapter commAdapter) {
        robotCommAdapter = commAdapter;
        tcpClientChannelManager = new TcpClientChannelManager<>(commAdapter,
                this::getChannelHandlers,
                commAdapter.getProcessModel().getVehicleIdleTimeout(),
                commAdapter.getProcessModel().isLoggingEnabled());
    }


    /**
     * 返回负责从字节流中写入和读取的通道处理程序
     *
     * @return 负责从字节流中写入和读取的通道处理程序
     */
    private List<ChannelHandler> getChannelHandlers() {
        return Arrays.asList(new LengthFieldBasedFrameDecoder(getMaxTelegramLength(), 1, 1, 2, 0),
                new VehicleTelegramDecoder(robotCommAdapter),
                new VehicleTelegramEncoder());
    }

    private int getMaxTelegramLength() {
        return Ints.max(OrderResponse.TELEGRAM_LENGTH,
                StateResponse.TELEGRAM_LENGTH);
    }

    @Override
    public void initialize() {
        tcpClientChannelManager.initialize();
    }

    @Override
    public void terminate() {
        tcpClientChannelManager.terminate();
    }

    @Override
    public void connect(String host, int port) {
        tcpClientChannelManager.connect(host, port);
    }

    @Override
    public void disconnect() {
        tcpClientChannelManager.disconnect();
    }

    @Override
    public boolean isConnected() {
        return tcpClientChannelManager.isConnected();
    }

    @Override
    public void setLoggingEnabled(boolean enabled) {
        tcpClientChannelManager.setLoggingEnabled(enabled);
    }

    @Override
    public void scheduleConnect(@Nonnull String host, int port, long delay) {
        tcpClientChannelManager.scheduleConnect(host, port, delay);
    }

    @Override
    public void send(Object telegram) {
        tcpClientChannelManager.send(telegram);
    }

    @Override
    public boolean isInitialize() {
        return tcpClientChannelManager.isInitialized();
    }
}
