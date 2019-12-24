package com.robot.mvc.dispatch;

import com.robot.agv.vehicle.telegrams.Protocol;

/**
 *  请求协议应答回复线程
 *  用于对请求到调试系统的请求进行应答回复
 *
 * @author Laotang
 */
public class AnswerHandler implements Runnable {

    private Protocol protocol;

    public AnswerHandler(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public void run() {

    }
}
