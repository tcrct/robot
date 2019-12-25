/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle.telegrams;

import static com.google.common.base.Preconditions.checkArgument;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.utils.ProtocolUtils;

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
   * The id of the point at the vehicle's current position.
   */
  private int positionId;
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
  private int lastFinishedOrderId;

  public StateResponse(Request request) {
    super(request.getProtocol());
    requireNonNull(request, "报文请求不能为空");

//    decodeTelegramContent(protocol);
  }

/**
 * Creates a new instance.
 *
 * @param protocol This telegram's raw content.
 */
  public StateResponse(Protocol protocol) {
    super(protocol);
    requireNonNull(protocol, "报文内容不能为空");

//    decodeTelegramContent(protocol);
  }
  /**
   * Returns the id of the point at the vehicle's current position.
   *
   * @return The id of the point at the vehicle's current position
   */
  public int getPositionId() {
    return positionId;
  }

  /**
   * Returns the vehicle's operating state.
   *
   * @return The vehicle's operating state.
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
  public int getLastFinishedOrderId() {
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
    return "StateResponse{" + "id=" + id + '}';
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

  private void decodeTelegramContent(String telegramData) {
//    id = Ints.fromBytes((byte) 0, (byte) 0, rawContent[3], rawContent[4]);
//    positionId = Ints.fromBytes((byte) 0, (byte) 0, rawContent[5], rawContent[6]);
//    operatingState = decodeOperatingState((char) rawContent[7]);
//    loadState = decodeLoadState((char) rawContent[8]);
//    lastReceivedOrderId = Ints.fromBytes((byte) 0, (byte) 0, rawContent[9], rawContent[10]);
//    currentOrderId = Ints.fromBytes((byte) 0, (byte) 0, rawContent[11], rawContent[12]);
//    lastFinishedOrderId = Ints.fromBytes((byte) 0, (byte) 0, rawContent[13], rawContent[14]);
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
   * The load handling state of a vehicle.
   */
  public static enum LoadState {
    /**
     * The vehicle's load handling state is currently empty.
     */
    EMPTY,
    /**
     * The vehicle's load handling state is currently full.
     */
    FULL,
    /**
     * The vehicle's load handling state is currently unknown.
     */
    UNKNOWN
  }

  /**
   * The operating state of a vehicle.
   */
  public static enum OperatingState {
    /**
     * The vehicle is currently executing an operation.
     */
    ACTING,
    /**
     * The vehicle is currently idle.
     */
    IDLE,
    /**
     * The vehicle is currently moving.
     */
    MOVING,
    /**
     * The vehicle is currently in an error state.
     */
    ERROR,
    /**
     * The vehicle is currently recharging.
     */
    CHARGING,
    /**
     * The vehicle's state is currently unknown.
     */
    UNKNOWN
  }
}
