package com.robot.mvc.interfaces;

import com.agvdcc.core.protocol.IProtocol;

/**
 * 发送报文协议接口
 *
 * @author Laotang
 */
public interface ITelegramSender {

    /**
     *  发送报文协议
     *
     * @param protocol The {@link IProtocol} to be sent.
     */
    void sendTelegram(IProtocol protocol);
}