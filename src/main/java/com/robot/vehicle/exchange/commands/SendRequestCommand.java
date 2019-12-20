/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.vehicle.exchange.commands;

import static java.util.Objects.requireNonNull;

import com.robot.common.telegrams.Request;
import com.robot.vehicle.RobotCommAdapter;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command for sending a telegram to the actual vehicle.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SendRequestCommand
    implements AdapterCommand {

  /**
   * The request to send.
   */
  private final Request request;

  /**
   * Creates a new instance.
   *
   * @param request The request to send.
   */
  public SendRequestCommand(Request request) {
    this.request = requireNonNull(request, "request");
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    if (!(adapter instanceof RobotCommAdapter)) {
      return;
    }

    RobotCommAdapter exampleAdapter = (RobotCommAdapter) adapter;
    exampleAdapter.getRequestResponseMatcher().enqueueRequest(request);
  }
}
