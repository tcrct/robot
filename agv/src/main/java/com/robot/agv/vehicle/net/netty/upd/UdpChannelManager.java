package com.robot.agv.vehicle.net.netty.upd;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.RobotCommAdapter;
import com.robot.agv.vehicle.net.netty.comm.VehicleTelegramDecoder;
import com.robot.agv.vehicle.net.netty.comm.VehicleTelegramEncoder;
import com.robot.agv.vehicle.net.IChannelManager;
import io.netty.channel.ChannelHandler;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * UPD方式
 */
public class UdpChannelManager implements IChannelManager<Request, Response> {

    private RobotCommAdapter robotCommAdapter;
    private UdpServerChannelManager udpServerChannelManager;


    public UdpChannelManager(RobotCommAdapter commAdapter) {
        robotCommAdapter = commAdapter;
        udpServerChannelManager = new UdpServerChannelManager(commAdapter,
                this::getChannelHandlers,
                commAdapter.getProcessModel().getVehicleIdleTimeout(),
                commAdapter.getProcessModel().isLoggingEnabled());
    }

    private List<ChannelHandler> getChannelHandlers() {
        return Arrays.asList(
                new VehicleTelegramDecoder(robotCommAdapter),
                new VehicleTelegramEncoder());
    }

    @Override
    public void initialize() {
        udpServerChannelManager.initialized();
    }

    @Override
    public boolean isInitialized() {
        return udpServerChannelManager.isInitialized();
    }

    @Override
    public void terminate() {
        udpServerChannelManager.terminate();
    }

    @Override
    public void connect(String host, int port) {
        udpServerChannelManager.connect(host, port);
    }

    @Override
    public void disconnect() {
        udpServerChannelManager.disconnect();
    }

    @Override
    public boolean isConnected() {
        return udpServerChannelManager.isConnected();
    }

    @Override
    public void setLoggingEnabled(boolean enable) {
        udpServerChannelManager.setLoggingEnabled(enable);
    }

    @Override
    public void scheduleConnect(@Nonnull String host, int port, long delay) {
        udpServerChannelManager.scheduleConnect(host, port, delay);
    }

    @Override
    public void send(Request telegram) {
        udpServerChannelManager.send(telegram);
    }
}
