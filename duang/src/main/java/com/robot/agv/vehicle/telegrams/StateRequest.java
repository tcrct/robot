/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle.telegrams;

import cn.hutool.core.util.IdUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.RobotProcessModel;
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
  private MovementCommand command;

  /**路径的最后一个点名称*/
  private String destinationId;
  /**动作名称*/
  private String destinationAction;
  /**工作名称*/
  private String destinationLocation;
  /**车辆内容2*/
  private RobotProcessModel model;

  /**
   * Creates a new instance.
   *
   * @param telegramId The request's telegram id.
   */
  public StateRequest(String telegramId) {
    super(new Protocol.Builder().build());
    this.id = telegramId;

//    encodeTelegramContent();
  }

  public StateRequest(Protocol protocol) {
    super(protocol);
    this.id = IdUtil.objectId();
  }

  /**
   * 构造函数
   * @param command opentcs的移动命令
   */
  public StateRequest(MovementCommand command, RobotProcessModel model) {
    this.command = command;
    this.model= model;
    encodeTelegramContent();
  }

  @Override
  public void updateRequestContent(Response response) {

//    encodeTelegramContent();
  }

  private void encodeTelegramContent() {
    this.id = IdUtil.objectId();
    destinationId = command.getFinalDestination().getName();
    destinationAction = command.getFinalOperation();
    destinationLocation = command.getFinalDestinationLocation().getName();
  }

  public MovementCommand getCommand() {
    return command;
  }

  public String getDestinationId() {
    return destinationId;
  }

  public String getDestinationAction() {
    return destinationAction;
  }

  public String getDestinationLocation() {
    return destinationLocation;
  }

  public RobotProcessModel getModel() {
    return model;
  }
}
