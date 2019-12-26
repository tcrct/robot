/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.common.telegrams;

import com.robot.utils.ProtocolUtils;
import com.robot.agv.vehicle.telegrams.Protocol;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;

/**
 * A response represents an answer of a vehicle control to a request sent by the control system.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public abstract class Response
    extends Telegram {

  /**
   * 构造函数
   *
   * @param protocol 协议对象
   */
  public Response(Protocol protocol) {
    super((null == protocol) ? "" : ProtocolUtils.converterString(protocol));
  }

  /**
   * Checks whether this is a response to the given request.
   * <p>
   * This implementation only checks for matching telegram ids.
   * Subclasses may want to extend this check.
   * </p>
   *
   * @param request The request to check with.
   * @return {@code true} if, and only if, the given request's id matches this response's id.
   */
  public boolean isResponseTo(@Nonnull Request request) {
    requireNonNull(request, "request");
    return request.getId().equals(getId());
  }

  public void write(Object object) {
    requireNonNull(object, "object");
    super.rawContent = String.valueOf(object);
  }

}
