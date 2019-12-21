package com.robot.agv.vehicle.net;

import javax.annotation.Nonnull;

public interface IChannelManager<I, O> {

    void initialize();

    boolean isInitialize();

    void terminate();

    void connect(String host, int port);

    void disconnect();

    boolean isConnected();

    void setLoggingEnabled(boolean enable);

    void scheduleConnect(@Nonnull String host, int port, long delay);

    void send(Object telegram);

}
