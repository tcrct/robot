/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle.telegrams;

import cn.hutool.core.util.IdUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.RobotProcessModel;
import com.robot.mvc.exceptions.RobotException;
import com.robot.numes.RobotEnum;
import com.robot.utils.ProtocolUtils;
import com.robot.utils.ToolsKit;
import org.opentcs.data.order.Route;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;


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
  private LinkedBlockingQueue<MovementCommand> commandQueue;

  private MovementCommand finalCommand;

  private boolean traffic;


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
  /**是否预停车*/
  private boolean isPreStop;


  /**
   * Creates a new instance.
   *
   * @param response 响应对象
   */
  public StateRequest(StateResponse response) {
    this(response.getProtocol());
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
   * @param commandQueue openTCS的移动命令
   * @param model openTCS的车辆模型对象
   */
  public StateRequest(LinkedBlockingQueue<MovementCommand> commandQueue, RobotProcessModel model) {
    this.commandQueue = commandQueue;
    this.model= model;
    super.id = IdUtil.objectId();
    encodeTelegramContent();
  }

  @Override
  public void updateRequestContent(Response response) {
    if (ToolsKit.isEmpty(response.getRawContent())) {
      throw new RobotException("返回的协议内容不能为空");
    }
    super.rawContent = response.getRawContent();
    super.protocol = ProtocolUtils.buildProtocol(rawContent);
    setPreStop();
  }

  public boolean isPreStop() {
    return isPreStop;
  }

  /**
   * 是否预停车指令
   * @return 是返回true
   */
  private void setPreStop() {
    if (ToolsKit.isEmpty(protocol) || ToolsKit.isEmpty(protocol.getParams())) {
      isPreStop = false;
    }
    String[] paramsArray = protocol.getParams().split(RobotEnum.PARAMLINK.getValue());
    String lastCmd = paramsArray[paramsArray.length-1];
    isPreStop =  lastCmd.startsWith(RobotEnum.PRE_STOP.getValue());
  }

  private void encodeTelegramContent() {
    super.id = IdUtil.objectId();
//    destinationId =
//    destinationAction =
//    destinationLocation =
    setRobotSend(true);
  }

  public MovementCommand getFinalCommand() {
    return finalCommand;
  }
  public void setFinalCommand(MovementCommand finalCommand) {
    this.finalCommand = finalCommand;
  }

  public LinkedBlockingQueue<MovementCommand> getCommandQueue() {
    return commandQueue;
  }

  public String getDestinationId() {
    return  finalCommand.getFinalDestination().getName();
  }

  public String getDestinationAction() {
    return finalCommand.getFinalOperation();
  }

  public String getDestinationLocation() {
    return finalCommand.getFinalDestinationLocation().getName();
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

  public boolean isTraffic() {
    return traffic;
  }

  public void setTraffic(boolean traffic) {
    this.traffic = traffic;
  }
}
