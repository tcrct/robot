package com.robot.mvc.dispatch;

import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.utils.ProtocolUtils;
import com.robot.agv.vehicle.telegrams.OrderRequest;
import com.robot.agv.vehicle.telegrams.Protocol;

/**
 *  请求协议应答回复线程
 *  用于对请求到调试系统的请求进行应答回复
 *
 * @author Laotang
 */
public class AnswerHandler implements Runnable {

    private Protocol protocol;
    private TelegramSender sender;

    public AnswerHandler(Protocol protocol, TelegramSender sender) {
        this.protocol = protocol;
        this.sender = sender;
    }

    @Override
    public void run() {
        // 凡是请求上报的(s)，均需要回
        boolean isNeedAnswer = ProtocolUtils.DIRECTION_REQUEST.equals(protocol.getDirection());
        if (isNeedAnswer) {
            protocol.setDirection(ProtocolUtils.DIRECTION_RESPONSE);
            protocol.setCrc(ProtocolUtils.builderCrcString(protocol));
            sender.sendTelegram(new OrderRequest(protocol));
        }
    }
}
