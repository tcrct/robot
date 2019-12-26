/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.common.telegrams;

import com.robot.utils.ProtocolUtils;
import com.robot.agv.vehicle.telegrams.Protocol;

/**
 * A request represents a telegram sent from the control system to vehicle control and expects
 * a response with the same id to match.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public abstract class Request
    extends Telegram {

  private Protocol protocol;
  /**验证码*/
  private String code;

  public Request() {
    super("");
  }

  /**
   *  构造函数
   *
   * @param protocol  协议对象
   */
  public Request(Protocol protocol) {
    super(ProtocolUtils.converterString(protocol));
    this.protocol = protocol;
  }

  public Protocol getProtocol() {
    return protocol;
  }

  /**
   * Updates the content of the request to include the given id.
   *
   * @param response  返回对象
   */
  public abstract void updateRequestContent(Response response);

  public String getCode() {
    if (null != protocol) {
      code = ProtocolUtils.builderCrcString(protocol);
    }
    return code;
  }

  @Override
  public String toString() {
    if (null != protocol) {
      return ProtocolUtils.converterString(protocol);
    }
    return "";
  }
}
