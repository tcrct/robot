package com.robot.core.handshake;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpStatus;
import com.robot.agv.common.send.SendRequest;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.vehicle.RobotCommAdapter;
import com.robot.agv.vehicle.RobotProcessModel;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.agv.vehicle.telegrams.StateRequest;
import com.robot.agv.vehicle.telegrams.StateResponse;
import com.robot.core.AppContext;
import com.robot.mvc.helper.ActionHelper;
import com.robot.service.common.requests.get.GetMtRequest;
import com.robot.service.common.requests.set.SetVmotRequest;
import com.robot.utils.ToolsKit;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.LongAdder;

/**
 * 报文监听器
 */
public class RobotTelegramListener implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(RobotTelegramListener.class);

    private List<String> deviceIds = new ArrayList<>();  // 车辆，设备ID
    private String vehicleId;

    public RobotTelegramListener(RobotCommAdapter adapter) {
        //缓存发送对象
        AppContext.setTelegramSender(adapter);
        vehicleId = adapter.getName();
        deviceIds.add(vehicleId);
        Set<String> deviceIdList = ActionHelper.duang().getVehicelDeviceMap().get(vehicleId);
        if (ToolsKit.isNotEmpty(deviceIdList)) {
            this.deviceIds.addAll(deviceIdList);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        sendCommandQueue();
        Iterator<Map.Entry<String, LinkedBlockingQueue<HandshakeTelegramDto>>> iterator = HandshakeTelegram.getHandshakeTelegram().entrySet().iterator();
        while (null != iterator && iterator.hasNext()) {
            Map.Entry<String, LinkedBlockingQueue<HandshakeTelegramDto>> entry = iterator.next();
            String key = entry.getKey();
            LinkedBlockingQueue<HandshakeTelegramDto> value = entry.getValue();
            if (ToolsKit.isNotEmpty(value) && peekTelegramQueueDto(value).isPresent()) {
                HandshakeTelegramDto queueDto = peekTelegramQueueDto(value).get();
                if (ToolsKit.isNotEmpty(queueDto)) {
                    ThreadUtil.execute(new ActionPerformedThread(key, queueDto));
//                    ActionPerformedThread thread = new ActionPerformedThread(key, queueDto);
//                    thread.run();
                }
            }
        }

        /*
        LOG.info("actionPerformed  " + String.valueOf(Thread.currentThread().getId()));
        for (String deviceId : deviceIds) {
            //间隔随机数50-100的毫秒时间，因为串口发送是单工的，所以需要间隔，即串口不能同时进行接收
            try {
                ActionPerformedThread actionPerformedThread = new ActionPerformedThread(deviceId,sender);
                actionPerformedThread.run();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }

         */

        /*
        ThreadUtil.execAsync(new Runnable() {
            @Override
            public void run() {
                LOG.debug("{}########报文监听器#########{}: ", vehicleId, deviceIds);

            }
        });

         */
    }

    private Optional<HandshakeTelegramDto> peekTelegramQueueDto(Queue<HandshakeTelegramDto> queue) {
        return Optional.ofNullable(queue.peek());
    }




    /**计数器*/
    private static final Map<String, LongAdder> LISTENER_COUNT_MAP = new HashMap<>();
    private static final int COUNT = 3;
    /**执行时间间隔*/
    int INTERVAL = 1000;
    /**
     * 清除提前上报动作请求
     *
     * @param protocol
     */
    /*
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
    */

    private void clearAdvanceReportTelegram(Request request,Protocol protocol) {
        LongAdder longAdder = LISTENER_COUNT_MAP.get(protocol.getDeviceId());
        if(null == longAdder) {
            longAdder = new LongAdder();
            LISTENER_COUNT_MAP.put(protocol.getDeviceId(), longAdder);
        }
        longAdder.increment();
        if(longAdder.intValue() == COUNT) {
            if (request.isActionResponse() && request.isRobotSend()) {
                if ("rptmt".equalsIgnoreCase(protocol.getCommandKey())) {
                    LOG.info("等待的是物料状态提交指令，发送getmt命令查询物料状态");
                    AppContext.getTelegramSender().sendTelegram(new GetMtRequest(protocol.getDeviceId(), "0"));
                }
                else if ("rptvmot".equalsIgnoreCase(protocol.getCommandKey())) {
                    LOG.info("等待的是动作到位状态提交指令，重发setvmot命令设置AGV动作");
                    AppContext.getTelegramSender().sendTelegram(new SetVmotRequest(protocol.getDeviceId(), protocol.getParams()));
                }
            }
            longAdder.reset();
        }
    }

    private boolean isNotSend;
    private RobotProcessModel processModel;
    private LinkedBlockingQueue<MovementCommand> commandQueue;
    public void addSendCommandQueue(RobotProcessModel processModel, LinkedBlockingQueue<MovementCommand> commandQueue) {
        this.commandQueue = commandQueue;
        this.processModel = processModel;
        isNotSend = true;
    }

    public void sendCommandQueue() {
        if ((null == commandQueue || commandQueue.isEmpty())) {
//            LOG.debug("sendCommandQueue commandQueue is null, exit...");
            return;
        }
        if (!isNotSend) {
            return;
        }
        LinkedBlockingQueue<MovementCommand> commands = new LinkedBlockingQueue<>(commandQueue.size());
        commands.addAll(commandQueue);
        MovementCommand lastCommand = null;
        for (MovementCommand command : commands) {
//            LOG.info("#########: {}", command);
            lastCommand = command;
        }
//        LOG.info("#$$$$$$$$$$$#adapter name: {}", processModel.getName());
        StateRequest stateRequest = new StateRequest(commands, processModel);
        // 设置为交通管制
        stateRequest.setTraffic(true);
        stateRequest.setFinalCommand(lastCommand);
        //进行业务处理
        StateResponse stateResponse = SendRequest.duang().send(stateRequest, AppContext.getTelegramSender());
        if (stateResponse.getStatus() != HttpStatus.HTTP_OK) {
            LOG.error("车辆[{}]进行业务处理里发生异常，退出处理!", processModel.getName());
            return;
        }
//        LOG.info("############stateResponse.rawContent: {} ", stateResponse.getRawContent());
        //自动重发机制
        RobotCommAdapter adapter = AppContext.getCommAdapter(processModel.getName());
        adapter.getRequestResponseMatcher().enqueueRequest(processModel.getName(), stateRequest);
        // 清空，防止多次重发
        commands.clear();
        adapter.getRequestResponseMatcher().clear();
        // 发送开关，已经发送
        isNotSend = false;
    }
}
