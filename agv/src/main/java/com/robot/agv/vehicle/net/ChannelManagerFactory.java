package com.robot.agv.vehicle.net;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.RobotCommAdapter;
import com.robot.agv.vehicle.net.serialport.SerialPortChannelManager;
import com.robot.agv.vehicle.net.tcp.TcpChannelManager;
import com.robot.agv.vehicle.net.upd.UdpChannelManager;

public class ChannelManagerFactory {


    /**
     * 取通讯渠道管理器
     * @param adapter   适配器
     * @param type  通讯类型枚举值
     * @return IChannelManager
     */
    public static IChannelManager<Request, Response> getManager(RobotCommAdapter adapter, NetChannelType type) {
        if (NetChannelType.TCP.equals(type)) {
            return new TcpChannelManager(adapter);
        }
        else if (NetChannelType.UDP.equals(type)) {
            return new UdpChannelManager(adapter);
        }
        else if (NetChannelType.SERIALPORT.equals(type)) {
            return new SerialPortChannelManager(adapter);
        }
        throw new NullPointerException("根据通讯类型枚举["+type.name()+"]没有找到实现类");
    }
}
