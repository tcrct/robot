/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle.exchange.commands;

import com.robot.agv.vehicle.RobotCommAdapter;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to set the adapter's state request interval.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SetStateRequestIntervalCommand
    implements AdapterCommand {

  /**
   * The new interval.
   */
  private final int interval;

  /**
   * Creates a new instance.
   *
   * @param interval The new interval
   */
  public SetStateRequestIntervalCommand(int interval) {
    this.interval = interval;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    if (!(adapter instanceof RobotCommAdapter)) {
      return;
    }

    RobotCommAdapter exampleAdapter = (RobotCommAdapter) adapter;
    exampleAdapter.getProcessModel().setStateRequestInterval(interval);
  }
}
