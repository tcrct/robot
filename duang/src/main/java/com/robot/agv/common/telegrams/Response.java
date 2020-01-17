/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.common.telegrams;

import javax.annotation.Nonnull;
import com.robot.agv.vehicle.telegrams.StateRequest;
import com.robot.mvc.exceptions.RobotException;
import com.robot.utils.ProtocolUtils;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.utils.RobotUtil;
import com.robot.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;

/**
 * A response represents an answer of a vehicle control to a request sent by the control system.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public abstract class Response extends Telegram {

  private static final Logger LOG = LoggerFactory.getLogger(Response.class);

  protected Protocol protocol;

  /**
   * 构造函数
   *
   * @param protocol 协议对象
   */
  public Response(Protocol protocol) {
    super((null == protocol) ? "" : ProtocolUtils.converterString(protocol));
  }

  /**
   * 比较ID是否一致
   *
   * @param request The request to check with.
   * @return {@code true} if, and only if, the given request's id matches this response's id.
   */
  public boolean isResponseTo(@Nonnull Request request) {
    requireNonNull(request, "request");
    return request.getId().equals(getId());
  }

  /**
   * 仅用于tryMatchWithCurrentRequest
   * @param request 队列中第一位的请求元素
   * @return
   */
  public  boolean containsPoint(@Nonnull Request request) {
    requireNonNull(request, "request");
    // 提交上来的点名称
    String postPoint = RobotUtil.getReportPoint(getProtocol());
    //
    String point = "";
    if ((request instanceof StateRequest) &&
            "setrout".equalsIgnoreCase(request.getProtocol().getCommandKey())) {
      StateRequest stateRequest = (StateRequest)request;
      point = stateRequest.getNextPointName();
    }
//    LOG.info("{}", request instanceof StateRequest);
//    LOG.info("cmdkey: {}", request.getProtocol().getCommandKey());
      boolean isOK = point.equals(postPoint);
      LOG.info("{}队列中第一位元素的点: {}， 提交上来的点: {}, 两个点匹配结果: {}",  request.getProtocol().getDeviceId(),
              point, postPoint, isOK);
      LOG.info("############containsPoint:  {}", ProtocolUtils.converterString(request.getProtocol()));

    return isOK;
  }

  /**
   * 写入发送的协议对象
   * @param object
   */
  public void write(Object object) {
    requireNonNull(object, "object");
    super.rawContent = String.valueOf(object);
    getProtocol();
  }

  /***
   * 设置状态码
   * @param status
   */
  public void setStatus(int status) {
    super.status = status;
  }


  @Override
  public String getCode() {
    return  (ToolsKit.isEmpty(code)) ? getProtocol().getCode() : code;
  }

  /**
   * 取车辆/设备名称
   * @return
   */
  public String getDeviceId() {
    return getProtocol().getDeviceId();
  }

  /**
   * 取握手报文的CODE
   * 即生成请求下发后，握手应答报文的code
   * @return
   */
  public String getHandshakeCode() {
    return ProtocolUtils.builderHandshakeCode(getProtocol());
  }

  /**
   * 协议对象
   * @return
   */
  public Protocol getProtocol() {
    if (ToolsKit.isEmpty(super.rawContent)) {
      throw new RobotException("返回的报文协议内容为空");
    }
    if (ToolsKit.isEmpty(protocol)) {
      protocol = ProtocolUtils.buildProtocol(super.rawContent);
    }
    return protocol;
  }

}
