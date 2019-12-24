/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle.telegrams;

import static com.google.common.base.Ascii.ETX;
import static com.google.common.base.Ascii.STX;
import com.google.common.primitives.Ints;
import com.robot.agv.common.telegrams.Request;
import org.opentcs.drivers.vehicle.MovementCommand;


/**
 * Represents a state request addressed to the vehicle.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class StateRequest extends Request {

  /**
   *  opentcs的车辆移动命令
   */
  public MovementCommand command;

  /**
   * The expected length of a telegram of this type.
   */
  public static final int TELEGRAM_LENGTH = 7;
  /**
   * The size of the payload (the raw content, without STX, SIZE, CHECKSUM and ETX).
   */
  public static final int PAYLOAD_LENGTH = TELEGRAM_LENGTH - 4;
  /**
   * The position of the checksum byte.
   */
  public static final int CHECKSUM_POS = TELEGRAM_LENGTH - 2;

  /**
   * Creates a new instance.
   *
   * @param telegramId The request's telegram id.
   */
  public StateRequest(String telegramId) {
    super(new Protocol.Builder().build());
    this.id = telegramId;

    encodeTelegramContent();
  }

  /**
   * 构造函数
   * @param command opentcs的移动命令
   */
  public StateRequest(MovementCommand command) {
    this.command = command;
    encodeTelegramContent();
  }

  @Override
  public void updateRequestContent(String telegramId) {
    id = telegramId;
    encodeTelegramContent();
  }

  private void encodeTelegramContent() {

  }
}
