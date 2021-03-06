/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle.telegrams;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.mvc.exceptions.RobotException;
import com.robot.utils.ProtocolUtils;
import com.robot.utils.ToolsKit;

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

  public OrderRequest(OrderResponse response) {
    this(response.getProtocol());
  }

  public OrderRequest(Protocol protocol) {
    super(protocol);
    this.setRobotSend(false);
    this.code = protocol.getCode();
    super.id = IdUtil.objectId();
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
    if (ToolsKit.isEmpty(response.getRawContent())) {
      throw new RobotException("返回的协议内容不能为空");
    }
    super.rawContent = response.getRawContent();
    super.protocol = ProtocolUtils.buildProtocol(rawContent);
  }


  private void encodeTelegramContent(Protocol protocol){

  }

}
