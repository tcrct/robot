package com.robot.agv.vehicle.net.serialport.rxtx;

/**
 * 串口存在有效数据监听
 */
public interface DataAvailableListener {
    /**
     * 串口存在有效数据
     */
    void dataAvailable();
}
