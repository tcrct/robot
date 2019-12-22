/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle.telegrams;

import com.google.common.primitives.Ints;
import com.robot.agv.common.telegrams.Response;

import static java.util.Objects.requireNonNull;

/**
 * Represents an order response sent from the vehicle.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class OrderResponse
    extends Response {

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
  private int orderId;

  /**
   * Creates a new instance.
   *
   * @param protocol This telegram's raw content.
   */
  public OrderResponse(Protocol protocol) {
    requireNonNull(protocol, "protocol");

    decodeTelegramContent();

  }

  /**
   * Returns the order id received by the vehicle.
   *
   * @return The order id received by the vehicle.
   */
  public int getOrderId() {
    return orderId;
  }

  @Override
  public String toString() {
    return "OrderResponse{" + "id=" + id + '}';
  }

  /**
   * 检查是否为状态类型的响应
   *
   * @param protocol 待检查的报文协议对象
   * @return 是则返回true，否则返回false
   */
  public static boolean isOrderResponse(Protocol protocol) {
    requireNonNull(protocol, "报文协议对象不能为空");

      String commandKey = protocol.getCommandKey();
      // 如果不是以下两种命令请求，则认为是其它动作指令请求
      return !"rptac".equalsIgnoreCase(commandKey) &&
                  !"rptrpt".equalsIgnoreCase(commandKey);
  }

  private void decodeTelegramContent() {
    this.id = Ints.fromBytes((byte) 0, (byte) 0, rawContent[3], rawContent[4]);
    orderId = Ints.fromBytes((byte) 0, (byte) 0, rawContent[5], rawContent[6]);
  }
}
