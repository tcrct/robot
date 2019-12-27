/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle.telegrams;

import cn.hutool.core.util.IdUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.RobotProcessModel;
import com.robot.mvc.exceptions.RobotException;
import com.robot.utils.ProtocolUtils;
import com.robot.utils.ToolsKit;
import org.opentcs.data.order.Route;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represents a state request addressed to the vehicle.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class StateRequest extends Request {

  private static final Logger LOG = LoggerFactory.getLogger(StateRequest.class);

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
  /**车辆内容*/
  private RobotProcessModel model;

  /**路径的当前点*/
  private String currectPointName;
  /**路径的下一个点*/
  private String nextPointName;


  /**
   * Creates a new instance.
   *
   * @param response 响应对象
   */
  public StateRequest(StateResponse response) {
    super(response.getProtocol());
    this.id = response.getId();
  }

  /**
   * 构造函数
   *
   * @param protocol 协议对象
   */
  public StateRequest(Protocol protocol) {
    super(protocol);
    this.id = IdUtil.objectId();
  }

  /**
   * 构造函数
   * @param command openTCS的移动命令
   * @param model openTCS的车辆模型对象
   */
  public StateRequest(MovementCommand command, RobotProcessModel model) {
    this.command = command;
    this.model= model;
    encodeTelegramContent();
  }

  @Override
  public void updateRequestContent(Response response) {
    if (ToolsKit.isEmpty(response.getRawContent())) {
      throw new RobotException("返回的协议内容不能为空");
    }
    super.rawContent = response.getRawContent();
    super.protocol = ProtocolUtils.buildProtocol(rawContent);
  }

  private void encodeTelegramContent() {
    super.id = IdUtil.objectId();
    destinationId = command.getFinalDestination().getName();
    destinationAction = command.getFinalOperation();
    destinationLocation = command.getFinalDestinationLocation().getName();
    Route.Step step = command.getStep();
    currectPointName = step.getSourcePoint().getName();
    nextPointName = step.getDestinationPoint().getName();
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

  public String getCurrectPointName() {
    return currectPointName;
  }

  public String getNextPointName() {
    return nextPointName;
  }
}
