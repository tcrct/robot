package com.robot.core.handshake;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.agv.vehicle.RobotCommAdapter;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.core.AppContext;
import com.robot.mvc.helper.ActionHelper;
import com.robot.service.common.requests.get.GetMtRequest;
import com.robot.service.common.requests.set.SetVmotRequest;
import com.robot.utils.ProtocolUtils;
import com.robot.utils.RobotUtil;
import com.robot.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.LongAdder;

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
        boolean isSleep = false;
        if (deviceIds!= null && deviceIds.size() >1) {
            isSleep = true;
        }
        for (String deviceId : deviceIds) {
            //间隔随机数50-100的毫秒时间，因为串口发送是单工的，所以需要间隔，即串口不能同时进行接收
            try {
                if (isSleep) {
                    Thread.sleep(RandomUtil.randomInt(50, 100));
                }
                doActionPerformed(deviceId);
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
        /*
        ThreadUtil.execAsync(new Runnable() {
            @Override
            public void run() {
                LOG.debug("{}########报文监听器#########{}: ", vehicleId, deviceIds);

            }
        });

         */
    }

    private void doActionPerformed(String key) {
        // 改用非阻塞队列
        ConcurrentLinkedQueue<HandshakeTelegramDto> queue = HandshakeTelegram.getHandshakeTelegramQueue(key);
        if (null == queue || queue.isEmpty()) {
            LOG.debug("车辆设备[{}]的报文监听器队列为空或不存在",key);
            return;
        }
        LOG.info("###key: {}, queue: {}", key, queue);
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

                        if (request.isActionResponse() && request.isRobotSend()) {
                            if ("rptmt".equalsIgnoreCase(protocol.getCommandKey())) {
                                LOG.info("等待的是物料状态提交指令，发送getmt命令查询物料状态");
                                sender.sendTelegram(new GetMtRequest(protocol.getDeviceId(), "0"));
                            } else if ("rptvmot".equalsIgnoreCase(protocol.getCommandKey())) {
                                LOG.info("等待的是动作到位状态提交指令，重发setvmot命令设置AGV动作");
                                sender.sendTelegram(new SetVmotRequest(protocol.getDeviceId(), protocol.getParams()));
                            }
                            clearAdvanceReportTelegram(protocol);
                        }
                    }
                }
            }
        }
    }

    private Optional<HandshakeTelegramDto> peekTelegramQueueDto(Queue<HandshakeTelegramDto> queue) {
        return Optional.ofNullable(queue.peek());
    }


    /**计数器*/
    private static final Map<String, LongAdder> LISTENER_COUNT_MAP = new HashMap<>();
    private static final int COUNT = 3;
    /**执行时间间隔*/
    int INTERVAL = 0;
    /**
     * 清除提前上报动作请求
     *
     * @param response
     */
    private void clearAdvanceReportTelegram(Protocol protocol) {
        LongAdder longAdder = LISTENER_COUNT_MAP.get(protocol.getDeviceId());
        if(null == longAdder) {
            longAdder = new LongAdder();
            LISTENER_COUNT_MAP.put(protocol.getDeviceId(), longAdder);
        }
        longAdder.increment();
        if(longAdder.intValue() == COUNT) {
            String key = protocol.getCode();

            LOG.info("等待 "+(INTERVAL*COUNT)+" ms后，没有收到回复，查询AppContext.getAdvanceReportMap是否存在已经验证码为["+key+"]的上报记录["+ ProtocolUtils.converterString(protocol)+"]");
            Protocol reportProtocol = AppContext.getAdvanceReportMap().get(key);
            if(ToolsKit.isNotEmpty(reportProtocol)) {
                LOG.info("验证码为["+key+"]的请求已经提前上报了，须移除握手队列里等待回复的请求，让程序后续操作");
                try {
                    HandshakeTelegram.duang().remove(protocol.getDeviceId(), key);
                    // 移除对应的记录，释放内存空间
                    AppContext.getAdvanceReportMap().remove(key);
                    LOG.info("移除握手队列里提前上报的请求["+key+"]且清理缓存集合成功");
                } catch (Exception e) {
                    LOG.info("清除提前上报的动作请求时出错: " + e.getMessage(), e);
                }
            } else {
                LOG.info("验证码为["+key+"]的上报请求不存在，继续等待上报");
            }
            longAdder.reset();
        }
    }
}
