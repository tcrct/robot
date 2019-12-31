package com.robot.core.handshake;

import cn.hutool.core.thread.ThreadUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.service.common.ActionRequest;
import com.robot.service.common.ActionResponse;
import com.robot.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(RobotTelegramListener.class);

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
                    // 如果不是等待上报请求，则重发指令
                    if(!request.isActionResponse()) {
                        sender.sendTelegram(request);
                    }
                    else {
                        Protocol protocol = response.getProtocol();
                        LOG.info("正在等待设备提交指令为["+protocol.getCommandKey()+"],握手验证码["+protocol.getCode()+"]的报文消息: " + response.getRawContent());
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
