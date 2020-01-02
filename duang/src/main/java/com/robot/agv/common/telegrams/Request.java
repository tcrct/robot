/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.common.telegrams;

import com.robot.utils.ProtocolUtils;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.utils.ToolsKit;

/**
 *  发送到车辆/设备的请求报文对象
 *  所有请求均继承该抽抽象类
 *
 * @author Laotang
 */
public abstract class Request extends Telegram {

  /**协议对象*/
  protected Protocol protocol;
  /**验证码*/
  private String code;
  /**是否服务器发送，为true是由服务器发送请求*/
  private boolean isRobotSend = false;
  /**是否为等待回复请求*/
  private boolean isActionResponse;

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
   * 根据响应结果，更新请求内容
   *
   * @param response  返回对象
   */
  public abstract void updateRequestContent(Response response);

  public String getCode() {
    if (null != getProtocol()) {
      code = ProtocolUtils.builderCrcString(protocol);
    }
    return code;
  }

  @Override
  public String toString() {
    if (null != protocol) {
      return ToolsKit.isEmpty(rawContent) ? ProtocolUtils.converterString(protocol) : rawContent;
    }
    return "";
  }

  public boolean isRobotSend() {
    return isRobotSend;
  }

  public void setRobotSend(boolean robotSend) {
    isRobotSend = robotSend;
  }

  public boolean isActionResponse() {
    return isActionResponse;
  }

  public void setActionResponse(boolean actionResponse) {
    isActionResponse = actionResponse;
  }
}
