package com.robot.agv.vehicle.net;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpStatus;
import com.robot.agv.common.send.SendRequest;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.agv.vehicle.net.netty.upd.UdpServerManager;
import com.robot.core.AppContext;
import com.robot.entity.Logs;
import com.robot.mvc.exceptions.RobotException;
import com.robot.mvc.helper.ActionHelper;
import com.robot.utils.*;
import com.robot.agv.vehicle.RobotCommAdapter;
import com.robot.agv.vehicle.net.serialport.SerialPortChannelManager;
import com.robot.agv.vehicle.net.netty.tcp.TcpChannelManager;
import com.robot.agv.vehicle.net.netty.upd.UdpClientManager;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.agv.vehicle.telegrams.StateResponse;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * 网络通讯管理工厂
 *
 * @author Laotang
 */
public class ChannelManagerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelManagerFactory.class);
    private static Object dispatchFactory = null;
    private static UdpServerManager udpServerManager = null;
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
//            return new UdpServerManager(adapter);
            if (null == udpServerManager) {
                udpServerManager = new UdpServerManager(adapter);
                String host = SettingUtils.getStringByGroup("host", NetChannelType.UDP.name().toLowerCase(), "0.0.0.0");
                Integer port = SettingUtils.getInt("port", NetChannelType.UDP.name().toLowerCase(), 9090);
                if (!udpServerManager.isInitialized()) {
                    udpServerManager.initialize();
                    if (!udpServerManager.isConnected()) {
                        udpServerManager.connect(host, port);
                        if (udpServerManager.isConnected()) {
                            LOG.info("Robot UDP服务器链接[{}:{}]成功", host, port);
                        } else {
                            LOG.info("Robot UDP服务器链接[{}:{}]失败", host, port);
                        }
                    }
                }
            }
//            LOG.info("连接车辆[{}], [{}]成功", adapter.getProcessModel().getName(),
//                    adapter.getProcessModel().getVehicleHost()+":"+adapter.getProcessModel().getVehiclePort());
            return new UdpClientManager(adapter);
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

        List<String> telegramDataList = ProtocolUtils.getTelegram2List(telegramData);
        if (ToolsKit.isEmpty(telegramDataList)) {
            return;
        }
        for (String data :  telegramDataList) {
            doTelegram(data);
        }

    }

    private static void doTelegram(String telegramData) {
        //将接收到的报文内容转换为Protocol对象
        Protocol protocol = null;
        try {
            protocol = ProtocolUtils.buildProtocol(telegramData);

            //如果协议对象为空或者不允许访问，则直接退出
            if (ToolsKit.isEmpty(protocol) || !ProtocolUtils.isAllowAccess(protocol.getDeviceId())) {
                return;
            }
            LOG.info("接收到的报文内容: " + telegramData);
        } catch (Exception e) {
            LOG.warn("将报文内容{}转换为Protocol对象时出错, 退出该请求的处理: {}, {}", telegramData,e.getMessage(), e);
        }

        String deviceId = protocol.getDeviceId();
        RobotCommAdapter adapter = AppContext.getCommAdapter(deviceId);
        if (ToolsKit.isEmpty(adapter)) {
            deviceId = RobotUtil.getAdapterByDeviceId(deviceId);
            adapter = AppContext.getCommAdapter(deviceId);
            if (ToolsKit.isEmpty(adapter)) {
                LOG.error("车辆[{}]对应的适配器不存在", deviceId);
                return;
            }
        }
        ConnectionEventListener<Response> eventListener = (ConnectionEventListener<Response>)adapter;
        TelegramSender telegramSender = AppContext.getTelegramSender();

        final Protocol finalProtocol = protocol;
        // 将请求转到业务逻辑处理
        FutureTask<Response> futureTask = (FutureTask<Response>) ThreadUtil.execAsync(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return SendRequest.duang().send(finalProtocol, telegramSender);
            }
        });

        Response response = null;
        try {
            response = futureTask.get();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        if (ToolsKit.isNotEmpty(response)) {
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                LOG.error("协议内容：{}，业务逻辑处理时发生异常，退出处理！", response.getRawContent());
                return;
            }
        } else {
            return;
        }

        protocol = response.getProtocol();
        if (ToolsKit.isEmpty(protocol)) {
            throw new RobotException("业务逻辑处理后的协议对象不能为空");
        }

        // 如果请求报文里包含rptac,rptrtp关键字，则认为是State请求
        // State请求是需要进入到RobotCommAdapter进行处理的，其它请求则直接进入到对应的车辆的Service处理
        if (ProtocolUtils.isRptacProtocol(protocol.getCommandKey()) ||
                ProtocolUtils.isRptrtpProtocol(protocol.getCommandKey())) {
            eventListener.onIncomingTelegram(new StateResponse(protocol));
        }
    }
}
