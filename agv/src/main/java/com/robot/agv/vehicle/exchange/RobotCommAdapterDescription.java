/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle.exchange;

import com.robot.agv.utils.SettingUtils;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

/**
 * 通讯适配器名称
 *
 * The example adapter's {@link VehicleCommAdapterDescription}.
 *
 * @author Laotang
 */
public class RobotCommAdapterDescription extends VehicleCommAdapterDescription {

  @Override
  public String getDescription() {
    return SettingUtils.getStringByGroup("name","adapter", "Robot");
  }
}
