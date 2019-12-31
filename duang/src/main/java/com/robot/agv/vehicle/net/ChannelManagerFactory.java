package com.robot.agv.vehicle.net;

import cn.hutool.http.HttpStatus;
import com.robot.agv.common.dispatching.DispatchAction;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.utils.ProtocolUtils;
import com.robot.agv.vehicle.RobotCommAdapter;
import com.robot.agv.vehicle.net.serialport.SerialPortChannelManager;
import com.robot.agv.vehicle.net.netty.tcp.TcpChannelManager;
import com.robot.agv.vehicle.net.netty.upd.UdpChannelManager;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.agv.vehicle.telegrams.StateResponse;
import com.robot.utils.ToolsKit;
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
    public static void onIncomingTelegram(ConnectionEventListener<Response> eventListener, TelegramSender telegramSender, String telegramData) {
        java.util.Objects.requireNonNull(telegramData, "报文协议内容不能为空");

        //将接收到的报文内容转换为Protocol对象
        Protocol protocol = null;
        try {
            protocol = ProtocolUtils.buildProtocol(telegramData);
        } catch (Exception e) {
            LOG.warn("将报文内容{}转换为Protocol对象时出错, 退出该请求的处理: {}, {}", telegramData,e.getMessage(), e);
        }

        // 将请求转到业务逻辑处理
        Response response = DispatchAction.duang().doAction(protocol, telegramSender);
        if (ToolsKit.isNotEmpty(response)) {
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                LOG.error("协议内容：{}，业务逻辑处理时发生异常，退出处理！", response.getRawContent());
                return;
            }
        } else {
            return;
        }
        protocol = response.getProtocol();
        // 如果请求报文里包含rptac,rptrtp关键字，则认为是State请求
        // State请求是需要进入到RobotCommAdapter进行处理的，其它请求则直接进入到对应的车辆的Service处理
        if (ToolsKit.isNotEmpty(protocol) && (
                ProtocolUtils.isRptacProtocol(protocol.getCommandKey()) ||
                        ProtocolUtils.isRptrtpProtocol(protocol.getCommandKey()))) {
            eventListener.onIncomingTelegram(new StateResponse(protocol));
        }
    }
}
