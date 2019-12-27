/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle.exchange.commands;

import static java.util.Objects.requireNonNull;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.vehicle.RobotCommAdapter;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * 发送请求电报到车辆
 *
 * @author Laotang
 */
public class SendRequestCommand
    implements AdapterCommand {

  /**
   * 请求报文
   */
  private final Request request;

  /**
   * 构造函数
   *
   * @param request 请求报文对象
   */
  public SendRequestCommand(Request request) {
    this.request = requireNonNull(request, "request");
  }


  /**发送*/
  @Override
  public void execute(VehicleCommAdapter adapter) {
    if (!(adapter instanceof RobotCommAdapter)) {
      return;
    }

    RobotCommAdapter robotCommAdapter = (RobotCommAdapter) adapter;
    robotCommAdapter.getRequestResponseMatcher().enqueueRequest(robotCommAdapter.getName(), request);
  }
}
