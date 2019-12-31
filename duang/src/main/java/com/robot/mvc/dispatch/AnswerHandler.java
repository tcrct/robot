package com.robot.mvc.dispatch;

import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.core.handshake.HandshakeTelegram;
import com.robot.numes.RobotEnum;
import com.robot.utils.ProtocolUtils;
import com.robot.agv.vehicle.telegrams.OrderRequest;
import com.robot.agv.vehicle.telegrams.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  请求协议应答回复线程
 *  用于对请求到调试系统的请求进行应答回复
 *
 * @author Laotang
 */
public class AnswerHandler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(AnswerHandler.class);

    private Protocol protocol;
    private TelegramSender sender;

    public AnswerHandler(Protocol protocol, TelegramSender sender) {
        this.protocol = protocol;
        this.sender = sender;
    }

    @Override
    public void run() {
        // 凡是请求上报的(s)，均需要应答回复
        if (RobotEnum.UP_LINK.getValue().equals(protocol.getDirection())) {
            //更改方向
            protocol.setDirection(ProtocolUtils.DIRECTION_RESPONSE);
            // 重新计算验证码
            protocol.setCode(ProtocolUtils.builderCrcString(protocol));
            OrderRequest orderRequest = new OrderRequest(protocol);
            LOG.info("发送应答报文[{}]", orderRequest.getRawContent());
            sender.sendTelegram(orderRequest);
        }
    }
}
