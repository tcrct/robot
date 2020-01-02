/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle.telegrams;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.numes.RobotEnum;
import com.robot.utils.CrcUtil;
import com.robot.utils.ProtocolUtils;
import com.robot.utils.RobotUtil;
import com.robot.utils.ToolsKit;

import static java.util.Objects.requireNonNull;

/**
 * Represents a vehicle status response sent from the vehicle.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class StateResponse
    extends Response {

  /**
   * The response type.
   */
  public static final byte TYPE = 1;
  /**
   * The expected length of a telegram of this type.
   */
  public static final int TELEGRAM_LENGTH = 17;
  /**
   * The size of the payload (the raw content, without STX, SIZE, CHECKSUM and ETX).
   */
  public static final int PAYLOAD_LENGTH = TELEGRAM_LENGTH - 4;
  /**
   * The position of the checksum byte.
   */
  public static final int CHECKSUM_POS = TELEGRAM_LENGTH - 2;
  /**
   * 上报的点
   */
  private String positionId;
  /**
   * The vehicle's operating state.
   */
  private OperatingState operatingState;
  /**
   * The vehicle's load state.
   */
  private LoadState loadState;
  /**
   * The id of the last received order.
   */
  private int lastReceivedOrderId;
  /**
   * The id of the current order.
   */
  private int currentOrderId;
  /**
   * The id of the last finished order.
   */
  private String lastFinishedOrderId;



  public StateResponse(Request request) {
    super(request.getProtocol());
    requireNonNull(request, "报文请求不能为空");
    super.id = request.getId();
    decodeTelegramContent(request.getProtocol());
  }

/**
 * Creates a new instance.
 *
 * @param protocol 协议对象
 */
  public StateResponse(Protocol protocol) {
    super(protocol);
    requireNonNull(protocol, "报文内容不能为空");
    if (ToolsKit.isNotEmpty(protocol.getCommandKey())) {
      this.positionId = RobotUtil.getReportPoint(protocol);
      this.lastFinishedOrderId = positionId;
    }
//    decodeTelegramContent(protocol);
  }
  /**
   * 取当前位置的卡号或ID号
   *
   * @return 车辆当前所在的位置
   */
  public String getPositionId() {
    return positionId;
  }

  /**
   * 返回车辆的操作状态
   *
   * @return 车辆的操作状态
   */
  public OperatingState getOperatingState() {
    return operatingState;
  }

  /**
   * Returns the vehicle's load state.
   *
   * @return The vehicle's load state.
   */
  public LoadState getLoadState() {
    return loadState;
  }

  /**
   * Returns the id of the last received order.
   *
   * @return The id of the last received order.
   */
  public int getLastReceivedOrderId() {
    return lastReceivedOrderId;
  }

  /**
   * Returns the id of the current order.
   *
   * @return The id of the current order.
   */
  public int getCurrentOrderId() {
    return currentOrderId;
  }

  /**
   * Returns the id of the last finished order.
   *
   * @return The id of the last finished order.
   */
  public String getLastFinishedOrderId() {
    return lastFinishedOrderId;
  }

  /**
   * Returns the telegram's checksum byte.
   *
   * @return The telegram's checksum byte.
   */
  public byte getCheckSum() {
    return getRawContentByte()[CHECKSUM_POS];
  }

  
  @Override
  public String toString() {
    return  rawContent;
  }

  /**
   * 检查是否为状态类型的响应
   * 方向为r时为响应
   *
   * @param protocol 待检查的报文协议对象
   * @return 是则返回true，否则返回false
   */
  public static boolean isStateResponse(Protocol protocol) {
    requireNonNull(protocol, "报文协议对象不能为空");

    return ProtocolUtils.isStateProtocol(protocol.getCommandKey()) &&
            ProtocolUtils.DIRECTION_RESPONSE.equalsIgnoreCase(protocol.getDirection());
  }

  private void decodeTelegramContent(Protocol reqProtocol) {
    if (ToolsKit.isEmpty(reqProtocol)) {
      return;
    }
    String direction = RobotEnum.UP_LINK.getValue().equalsIgnoreCase(reqProtocol.getDirection())
            ? RobotEnum.DOWN_LINK.getValue() : reqProtocol.getDirection();
    super.protocol  = null;
    super.protocol  = new Protocol.Builder()
            .commandKey(reqProtocol.getCommandKey())
            .direction(direction)
            .params(reqProtocol.getParams())
            .deviceId(reqProtocol.getDeviceId())
            .build();
    super.rawContent = ProtocolUtils.converterString(super.protocol);
    String code = CrcUtil.CrcVerify_Str(ProtocolUtils.builderCrcString(super.protocol));
    super.protocol.setCode(code);
    super.code = code;
  }

  private OperatingState decodeOperatingState(char operatingStateRaw) {
    switch (operatingStateRaw) {
      case 'A':
        return OperatingState.ACTING;
      case 'I':
        return OperatingState.IDLE;
      case 'M':
        return OperatingState.MOVING;
      case 'E':
        return OperatingState.ERROR;
      case 'C':
        return OperatingState.CHARGING;
      default:
        return OperatingState.UNKNOWN;
    }
  }

  private LoadState decodeLoadState(char loadStateRaw) {
    switch (loadStateRaw) {
      case 'E':
        return LoadState.EMPTY;
      case 'F':
        return LoadState.FULL;
      default:
        return LoadState.UNKNOWN;
    }
  }

  /**
   * 车辆的负载处理状态
   */
  public static enum LoadState {
    /**
     * 空
     */
    EMPTY,
    /**
     * 已装载
     */
    FULL,
    /**
     * 未知
     */
    UNKNOWN
  }

  /**
   * The operating state of a vehicle.
   */
  public static enum OperatingState {
    /**
     * 正在执行操作
     */
    ACTING,

    /**
     * 空闲
     */
    IDLE,

    /**
     * 移动
     */
    MOVING,

    /**
     * 错误
     */
    ERROR,
    /**
     * 充电
     */
    CHARGING,
    /**
     * 未知
     */
    UNKNOWN
  }
}
