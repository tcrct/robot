/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.vehicle.exchange.commands;

import static java.util.Objects.requireNonNull;

import com.robot.vehicle.RobotCommAdapter;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to set the vehicle's host.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SetVehicleHostCommand
    implements AdapterCommand {

  /**
   * The host to set.
   */
  private final String host;

  /**
   * Creates a new instnace.
   *
   * @param host The host to set.
   */
  public SetVehicleHostCommand(String host) {
    this.host = requireNonNull(host, "host");
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    if (!(adapter instanceof RobotCommAdapter)) {
      return;
    }

    RobotCommAdapter exampleAdapter = (RobotCommAdapter) adapter;
    exampleAdapter.getProcessModel().setVehicleHost(host);
  }
}
