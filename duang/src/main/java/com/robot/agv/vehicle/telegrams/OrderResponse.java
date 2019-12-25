/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle.telegrams;


import cn.hutool.core.util.IdUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.utils.ProtocolUtils;

import static java.util.Objects.requireNonNull;

/**
 * Represents an order response sent from the vehicle.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class OrderResponse extends Response {

  /**
   * The response type.
   */
  public static final byte TYPE = 2;
  /**
   * The expected length of a telegram of this type.
   */
  public static final int TELEGRAM_LENGTH = 9;
  /**
   * The size of the payload (the raw content, without STX, SIZE, CHECKSUM and ETX).
   */
  public static final int PAYLOAD_LENGTH = TELEGRAM_LENGTH - 4;
  /**
   * The position of the checksum byte.
   */
  public static final int CHECKSUM_POS = TELEGRAM_LENGTH - 2;
  /**
   * The order id received by the vehicle.
   */
  private String code;

  /**
   * Creates a new instance.
   *
   * @param protocol  协议对象
   */
  public OrderResponse(Protocol protocol) {
    super(protocol);
    decodeTelegramContent();
  }

  public OrderResponse(Request request) {
    super(request.getProtocol());
    super.id = request.getId();
//    decodeTelegramContent();
  }

  /**
   * Returns the order id received by the vehicle.
   *
   * @return The order id received by the vehicle.
   */
  public String getCode() {
    return code;
  }

  @Override
  public String toString() {
    return "OrderResponse{" + "id=" + id + '}';
  }

  /**
   * 检查是否为状态类型的响应
   *方向为r时为响应
   *
   * @param protocol 待检查的报文协议对象
   * @return 是则返回true，否则返回false
   */
  public static boolean isOrderResponse(Protocol protocol) {
    requireNonNull(protocol, "报文协议对象不能为空");

    return ProtocolUtils.isOrderProtocol(protocol.getCommandKey()) &&
                ProtocolUtils.DIRECTION_RESPONSE.equalsIgnoreCase(protocol.getDirection());
  }

  private void decodeTelegramContent() {
    super.id = IdUtil.objectId();

  }

}
