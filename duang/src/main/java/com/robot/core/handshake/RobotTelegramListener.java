package com.robot.core.handshake;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.agv.vehicle.RobotCommAdapter;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.mvc.helper.ActionHelper;
import com.robot.service.common.requests.get.GetMtRequest;
import com.robot.utils.RobotUtil;
import com.robot.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 报文监听器
 */
public class RobotTelegramListener implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(RobotTelegramListener.class);

    private TelegramSender sender;
    private List<String> deviceIds = new ArrayList<>();  // 车辆，设备ID
    private String vehicleId;

    public RobotTelegramListener(RobotCommAdapter adapter) {
        this.sender = adapter;
        vehicleId = adapter.getName();
        deviceIds.add(vehicleId);
        Set<String> deviceIdList = ActionHelper.duang().getVehicelDeviceMap().get(vehicleId);
        if (ToolsKit.isNotEmpty(deviceIdList)) {
            this.deviceIds.addAll(deviceIdList);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ThreadUtil.execAsync(new Runnable() {
            @Override
            public void run() {
                LOG.debug("{}########报文监听器#########{}: ", vehicleId, deviceIds);
                for (String deviceId : deviceIds) {
                    //间隔随机数，因为串口发送是单工的，所以需要间隔，即串口不能同时进行接收
                    try {
                        Thread.sleep(RandomUtil.randomInt(50, 100));
                        doActionPerformed(deviceId);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void doActionPerformed(String key) {
        LinkedBlockingQueue<HandshakeTelegramDto> queue = HandshakeTelegram.getHandshakeTelegramQueue(key);
        if (null == queue || queue.isEmpty()) {
            LOG.debug("车辆设备[{}]的报文监听器队列为空或不存在",key);
            return;
        }
        Iterator<HandshakeTelegramDto> iterator = queue.iterator();
        while (null != iterator && iterator.hasNext()) {
            HandshakeTelegramDto queueDto = iterator.next();
            if(ToolsKit.isNotEmpty(queueDto)){
//                HandshakeTelegramDto queueDto = peekTelegramQueueDto(value).get();
                Request request = queueDto.getRequest();
                Response response = queueDto.getResponse();
                if(ToolsKit.isNotEmpty(queueDto) && ToolsKit.isNotEmpty(request) && ToolsKit.isNotEmpty(response)) {
                    // 如果不是等待上报请求，则重发指令
                    if(!request.isActionResponse()) {
                        sender.sendTelegram(request);
                    }
                    else {
                        Protocol protocol = response.getProtocol();
                        LOG.info("正在等待设备提交指令为["+protocol.getCommandKey()+"],握手验证码为["+protocol.getCode()+"]的报文消息: " + response.getRawContent());

                        if (request.isActionResponse() && request.isRobotSend() &&
                                ("rptmt".equalsIgnoreCase(protocol.getCommandKey())
                                        || "rptvmot".equalsIgnoreCase(protocol.getCommandKey())
                                )
                        ) {
//                            RobotUtil.checkSensorStatus(protocol);
                            LOG.info("等待的是物料状态提交指令，发送getmt命令查询物料状态");
                            sender.sendTelegram(new GetMtRequest(protocol.getDeviceId(), "0"));
                        }
//                        clearAdvanceReportTelegram(response);
                    }
                }
            }
        }
    }

    private Optional<HandshakeTelegramDto> peekTelegramQueueDto(Queue<HandshakeTelegramDto> queue) {
        return Optional.ofNullable(queue.peek());
    }
}
