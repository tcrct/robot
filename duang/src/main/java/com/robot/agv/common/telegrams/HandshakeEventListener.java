//package com.robot.agv.common.telegrams;
//
//
//
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.awt.event.ActionEvent;
//import java.util.*;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.atomic.LongAdder;
//
///**
// * 报文监听器，负责发送存在队列中的报文
// * 对所有的请求与响应都需要进行握手监听
// *
// * @author Laotang
// */
//public class HandshakeEventListener implements IHandshakeListener {
//
//    private static final Logger logger = LoggerFactory.getLogger(HandshakeEventListener.class);
//
//    private static ITelegramSender sender;
//    private static HandshakeTelegramQueue handshakeTelegramQueue;
//    private static HandshakeEventListener listener = new HandshakeEventListener();
//    /**计数器*/
//    private static final Map<String, LongAdder> LISTENER_COUNT_MAP = new HashMap<>();
//    private static final int COUNT = 3;
//    /**执行时间间隔*/
//    int INTERVAL = 0;
//
//    public static HandshakeEventListener duang() {
//        return listener;
//    }
//
//    private HandshakeEventListener() {
//        handshakeTelegramQueue = AppContext.getAgvConfigure().getHandshakeTelegramQueue();
//        INTERVAL = SettingUtils.getInt("handshake.interval", "adapter", 1000);
//    }
//
//    @Override
//    public void setSender(ITelegramSender sender) {
//        Objects.requireNonNull(sender, "ITelegramSender is null");
//        this.sender = sender;
//    }
//
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        Iterator<Map.Entry<String, LinkedBlockingQueue<HandshakeTelegramDto>>> iterator = handshakeTelegramQueue.getIterator();
//        while (null != iterator && iterator.hasNext()) {
//            Map.Entry<String, LinkedBlockingQueue<HandshakeTelegramDto>> entry = iterator.next();
//            LinkedBlockingQueue<HandshakeTelegramDto> value = entry.getValue();
//            if(ToolsKit.isNotEmpty(value) && peekTelegramQueueDto(value).isPresent()){
//                HandshakeTelegramDto queueDto = peekTelegramQueueDto(value).get();
//                IResponse response = queueDto.getResponse();
//                if(ToolsKit.isNotEmpty(queueDto) && ToolsKit.isNotEmpty(response)) {
//                    // 如果是服务器发送的，则需要监听发送
//                    if(response.isHandshakeList()) {
//                        sender.sendTelegram(response);
//                    } else {
//                        logger.info("正在等待设备提交指令为["+response.getCmdKey()+"],握手验证码["+response.getHandshakeKey()+"]的报文消息: " + response.toString());
//                        clearAdvanceReportTelegram(response);
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
//    /**
//     * 清除提前上报动作请求
//     *
//     * @param response
//     */
//    private void clearAdvanceReportTelegram(IResponse response) {
//        LongAdder longAdder = LISTENER_COUNT_MAP.get(response.getDeviceId());
//        if(null == longAdder) {
//            longAdder = new LongAdder();
//            LISTENER_COUNT_MAP.put(response.getDeviceId(), longAdder);
//        }
//        longAdder.increment();
//        if(longAdder.intValue() == COUNT) {
//             String key = response.getHandshakeKey();
//            logger.info("等待 "+(INTERVAL*COUNT)+" ms后，没有收到回复，查询AppContext.getAdvanceReportMap是否存在已经验证码为["+key+"]的上报记录["+response.toString()+"]");
//            IRequest request = AppContext.getAdvanceReportMap().get(key);
//            if(ToolsKit.isNotEmpty(request)) {
//                logger.info("验证码为["+key+"]的请求已经提前上报了，须移除握手队列里等待回复的请求，让程序后续操作");
//                try {
//                    handshakeTelegramQueue.remove(response.getDeviceId(), response.getHandshakeKey());
//                    // 移除对应的记录，释放内存空间
//                    AppContext.getAdvanceReportMap().remove(key);
//                    logger.info("移除握手队列里提前上报的请求["+key+"]且清理缓存集合成功");
//                } catch (Exception e) {
//                    logger.info("清除提前上报的动作请求时出错: " + e.getMessage(), e);
//                }
//            } else {
//                logger.info("验证码为["+key+"]的上报请求不存在，继续等待上报");
//            }
//            longAdder.reset();
//        }
//    }
//}
