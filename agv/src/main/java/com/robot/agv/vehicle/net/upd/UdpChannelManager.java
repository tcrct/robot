package com.robot.agv.vehicle.net.upd;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.RobotCommAdapter;
import com.robot.agv.vehicle.net.IChannelManager;

import javax.annotation.Nonnull;

/**
 * UPD方式
 */
public class UdpChannelManager implements IChannelManager<Request, Response> {

    private RobotCommAdapter robotCommAdapter;

    public UdpChannelManager(RobotCommAdapter commAdapter) {
        robotCommAdapter = commAdapter;
    }

    @Override
    public void initialize() {

    }

    @Override
    public boolean isInitialize() {
        return false;
    }

    @Override
    public void terminate() {

    }

    @Override
    public void connect(String host, int port) {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void setLoggingEnabled(boolean enable) {

    }

    @Override
    public void scheduleConnect(@Nonnull String host, int port, long delay) {

    }

    @Override
    public void send(Object telegram) {

    }
}
