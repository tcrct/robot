/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle.telegrams;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.utils.ProtocolUtils;

import static java.util.Objects.requireNonNull;

/**
 * Represents an order request addressed to the vehicle.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class OrderRequest extends Request {

  /**
   * The request type.
   */
  public static final byte TYPE = 2;
  /**
   * The expected length of a telegram of this type.
   */
  public static final int TELEGRAM_LENGTH = 12;
  /**
   * The size of the payload (the raw content, without STX, SIZE, CHECKSUM and ETX).
   */
  public static final int PAYLOAD_LENGTH = TELEGRAM_LENGTH - 4;
  /**
   * The position of the checksum byte.
   */
  public static final int CHECKSUM_POS = TELEGRAM_LENGTH - 2;
  /**
   * crc验证码
   */
  private String code;
  /**
   * The name of the destination point.
   */
  private int destinationId;
  /**
   * The action to execute at the destination point.
   */
  private OrderAction destinationAction;

  public OrderRequest(Protocol protocol) {
    super(protocol);
    this.code = protocol.getCrc();
  }

  /**
   * Returns this telegram's order orderId.
   *
   * @return This telegram's order orderId
   */
  public String getCode() {
    return code;
  }

  /**
   * Returns this telegram's destination name.
   *
   * @return This telegram's destination name
   */
  public int getDestinationId() {
    return destinationId;
  }

  /**
   * Returns this telegram's destination action.
   *
   * @return This telegram's destination action
   */
  public OrderAction getDestinationAction() {
    return destinationAction;
  }

  @Override
  public void updateRequestContent(Response response) {
    encodeTelegramContent(null);
  }


  private void encodeTelegramContent(Protocol protocol){

  }

  /**
   * 检查是否为状态类型的请求，
   * 方向为s时为请求
   *
   * @param protocol 待检查的协议对象
   * @return 是则返回true，否则返回false
   */
  public static boolean isOrderRequest(Protocol protocol) {
    requireNonNull(protocol, "报文协议对象不能为空");
    return ProtocolUtils.isOrderProtocol(protocol.getCommandKey()) &&
                    ProtocolUtils.DIRECTION_REQUEST.equalsIgnoreCase(protocol.getDirection());
  }
}
