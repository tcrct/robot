/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.vehicle.exchange;

import java.util.ResourceBundle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

/**
 * The example adapter's {@link VehicleCommAdapterDescription}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class RobotCommAdapterDescription
    extends VehicleCommAdapterDescription {

  @Override
  public String getDescription() {
    return ResourceBundle.getBundle("de/fraunhofer/iml/opentcs/example/commadapter/Bundle").
        getString("ExampleAdapterFactoryDescription");
  }
}
