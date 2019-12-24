package com.robot.agv.vehicle.net;

import cn.hutool.core.util.ReflectUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.utils.ProtocolUtils;
import com.robot.agv.utils.SettingUtils;
import com.robot.agv.vehicle.RobotCommAdapter;
import com.robot.agv.vehicle.net.serialport.SerialPortChannelManager;
import com.robot.agv.vehicle.net.netty.tcp.TcpChannelManager;
import com.robot.agv.vehicle.net.netty.upd.UdpChannelManager;
import com.robot.agv.vehicle.telegrams.OrderRequest;
import com.robot.agv.vehicle.telegrams.OrderResponse;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.agv.vehicle.telegrams.StateResponse;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 网络通讯管理工厂
 *
 * @author Laotang
 */
public class ChannelManagerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelManagerFactory.class);
    private static Object dispatchFactory = null;
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

    /**
     * 接收报文消息
     * @param eventListener
     * @param telegramData
     */
    public static void onIncomingTelegram(ConnectionEventListener<Response> eventListener, String telegramData) {
        java.util.Objects.requireNonNull(telegramData, "报文协议内容不能为空");
        initDispatchFactory();
        //将接收到的报文内容转换为Protocol对象
        Protocol protocol = null;
        try {
            protocol = ProtocolUtils.buildProtocol(telegramData);
        } catch (Exception e) {
            LOG.warn("将报文内容{}转换为Protocol对象时出错, 退出该请求的处理: {}, {}", telegramData,e.getMessage(), e);
        }

        // 如果是Order请求或响应(车辆主动上报的)，则直接进行到车辆或设备的Service
        if (OrderRequest.isOrderRequest(protocol)) {
//            eventListener.onIncomingTelegram(new OrderRequest(protocol));
        }
        // 如果是Order响应(车辆回复)
        else if (OrderResponse.isOrderResponse(protocol)) {
            Response response = ReflectUtil.invoke(dispatchFactory, "execute", new OrderResponse(protocol));
            eventListener.onIncomingTelegram(response);
        } else {
            LOG.error("该报文不符合规则：{}", telegramData);
        }


        // 如果请求报文里包含setrout,rptac,rptrtp关键字，则认为是State请求
        //State请求是需要进入到RobotCommAdapter进行处理的，其它请求则直接进入到对应的车辆的Service处理
        if (StateResponse.isStateResponse(protocol)) {
            eventListener.onIncomingTelegram(new StateResponse(protocol));
        }
    }

    private static void initDispatchFactory() {
        try {
            if(null == dispatchFactory) {
                dispatchFactory = ReflectUtil.newInstance(
                        SettingUtils.getString("dispatch.factory","com.robot.mvc.dispatch.DispatchFactory"));
            }
        } catch (Exception e) {
            LOG.error("初始化业务处理分发工厂类时出错: {}, {}", e.getMessage(), e);
        }
    }

}
