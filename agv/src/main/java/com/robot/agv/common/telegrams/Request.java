/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.common.telegrams;

import com.robot.agv.utils.ProtocolUtils;
import com.robot.agv.vehicle.telegrams.Protocol;

/**
 * A request represents a telegram sent from the control system to vehicle control and expects
 * a response with the same id to match.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public abstract class Request
    extends Telegram {

  /**
   *  构造函数
   *
   * @param protocol  协议对象
   */
  public Request(Protocol protocol) {
    super(ProtocolUtils.converterString(protocol));
  }

  /**
   * Updates the content of the request to include the given id.
   *
   * @param telegramId The request's new id.
   */
  public abstract void updateRequestContent(int telegramId);
}
