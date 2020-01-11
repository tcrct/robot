//package com.robot.core.handshake;
//
//import com.robot.agv.common.telegrams.Request;
//import com.robot.agv.common.telegrams.Response;
//import com.robot.agv.common.telegrams.TelegramSender;
//import com.robot.agv.vehicle.telegrams.Protocol;
//import com.robot.core.AppContext;
//import com.robot.service.common.requests.get.GetMtRequest;
//import com.robot.service.common.requests.set.SetVmotRequest;
//import com.robot.utils.ProtocolUtils;
//import com.robot.utils.ToolsKit;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.*;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.atomic.LongAdder;
//
//public class ActionPerformedThread extends Thread {
//
//    private static final Logger LOG = LoggerFactory.getLogger(ActionPerformedThread.class);
//
//    private String key;
//    private TelegramSender sender;
//
//    public ActionPerformedThread(String key, TelegramSender sender) {
//        this.key = key;
//        this.sender = sender;
//    }
//
//    @Override
//    public void run() {
//        doActionPerformed(key);
//    }
//
//    private void doActionPerformed(String key) {
//        // 改用非阻塞队列
//        LinkedBlockingQueue<HandshakeTelegramDto> queue = HandshakeTelegram.getHandshakeTelegramQueue(key);
//        if (null == queue || queue.isEmpty()) {
//            LOG.debug("车辆设备[{}]的报文监听器队列为空或不存在",key);
//            return;
//        }
//        LOG.info("time: {}    threadName: {}  ###key: {}, queue: {},  hashCode: {}", System.currentTimeMillis(), this.getId(), key, queue, this.hashCode());
//        Iterator<HandshakeTelegramDto> iterator = queue.iterator();
//        while (null != iterator && iterator.hasNext()) {
//            HandshakeTelegramDto queueDto = iterator.next();
//            if(ToolsKit.isNotEmpty(queueDto)){
////                HandshakeTelegramDto queueDto = peekTelegramQueueDto(value).get();
//                Request request = queueDto.getRequest();
//                Response response = queueDto.getResponse();
//                if(ToolsKit.isNotEmpty(queueDto) && ToolsKit.isNotEmpty(request) && ToolsKit.isNotEmpty(response)) {
//                    // 如果不是等待上报请求，则重发指令
//                    if(!request.isActionResponse()) {
//                        sender.sendTelegram(request);
//                    }
//                    else {
//                        Protocol protocol = response.getProtocol();
//                        LOG.info("正在等待设备提交指令为["+protocol.getCommandKey()+"],握手验证码为["+protocol.getCode()+"]的报文消息: " + response.getRawContent());
//                        clearAdvanceReportTelegram(request, protocol);
//
//                    }
//                }
//            }
//        }
//    }
//
//    private Optional<HandshakeTelegramDto> peekTelegramQueueDto(Queue<HandshakeTelegramDto> queue) {
//        return Optional.ofNullable(queue.peek());
//    }
//
//    /**计数器*/
//    private static final Map<String, LongAdder> LISTENER_COUNT_MAP = new HashMap<>();
//    private static final int COUNT = 3;
//    /**执行时间间隔*/
//    int INTERVAL = 1000;
//    private void clearAdvanceReportTelegram(Request request,Protocol protocol) {
//        LongAdder longAdder = LISTENER_COUNT_MAP.get(protocol.getDeviceId());
//        if(null == longAdder) {
//            longAdder = new LongAdder();
//            LISTENER_COUNT_MAP.put(protocol.getDeviceId(), longAdder);
//        }
//        longAdder.increment();
//        if(longAdder.intValue() == COUNT) {
//            if (request.isActionResponse() && request.isRobotSend()) {
//                if ("rptmt".equalsIgnoreCase(protocol.getCommandKey())) {
//                    LOG.info("等待的是物料状态提交指令，发送getmt命令查询物料状态");
//                    sender.sendTelegram(new GetMtRequest(protocol.getDeviceId(), "0"));
//                }
//                else if ("rptvmot".equalsIgnoreCase(protocol.getCommandKey())) {
//                    LOG.info("等待的是动作到位状态提交指令，重发setvmot命令设置AGV动作");
//                    sender.sendTelegram(new SetVmotRequest(protocol.getDeviceId(), protocol.getParams()));
//                }
//            }
//            longAdder.reset();
//        }
//    }
//}
