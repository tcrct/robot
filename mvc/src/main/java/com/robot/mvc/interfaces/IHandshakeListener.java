package com.robot.mvc.interfaces;

import java.awt.event.ActionListener;

public interface IHandshakeListener extends ActionListener {
    void setSender(ITelegramSender sender);
}
