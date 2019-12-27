package com.robot.core.handshake;

import cn.hutool.core.thread.ThreadUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.utils.ToolsKit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 报文监听器
 */
public class RobotTelegramListener implements ActionListener {

    private TelegramSender sender;

    public RobotTelegramListener(TelegramSender telegramSender) {
        this.sender = telegramSender;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<Map.Entry<String, LinkedBlockingQueue<HandshakeTelegramDto>>> iterator = HandshakeTelegram.getHandshakeTelegramQueue().entrySet().iterator();
        while (null != iterator && iterator.hasNext()) {
            Map.Entry<String, LinkedBlockingQueue<HandshakeTelegramDto>> entry = iterator.next();
            LinkedBlockingQueue<HandshakeTelegramDto> value = entry.getValue();
            if(ToolsKit.isNotEmpty(value) && peekTelegramQueueDto(value).isPresent()){
                HandshakeTelegramDto queueDto = peekTelegramQueueDto(value).get();
                Request request = queueDto.getRequest();
                Response response = queueDto.getResponse();
                if(ToolsKit.isNotEmpty(queueDto) && ToolsKit.isNotEmpty(request) && ToolsKit.isNotEmpty(response)) {
                    // 如果是服务器发送的，则需要监听发送
    //                            if(response.isHandshakeList()) {
                        sender.sendTelegram(request);
    //                            }
    //                            else {
    //                                logger.info("正在等待设备提交指令为["+response.getCmdKey()+"],握手验证码["+response.getHandshakeKey()+"]的报文消息: " + response.toString());
    //                                clearAdvanceReportTelegram(response);
    //                            }
                }
            }
        }
    }

    private Optional<HandshakeTelegramDto> peekTelegramQueueDto(Queue<HandshakeTelegramDto> queue) {
        return Optional.ofNullable(queue.peek());
    }
}
