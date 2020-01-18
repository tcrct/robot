/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle;

import com.robot.agv.common.telegrams.BoundedCounter;
import com.robot.agv.vehicle.telegrams.StateRequest;
import org.opentcs.data.model.Point;
import org.opentcs.drivers.vehicle.MovementCommand;

import static com.robot.agv.common.telegrams.BoundedCounter.UINT16_MAX_VALUE;

/**
 * 将{@link MovementCommand}从openTCS映射到发送车辆的电报。
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class StateMapper {

  /**
   * Counts the order id's sent to the vehicle.
   */
  private final BoundedCounter orderIdCounter = new BoundedCounter(1, UINT16_MAX_VALUE);

  /**
   * Creates a new instance.
   */
  public StateMapper() {
  }

  /**
   * Maps the given command to an order request that can be sent to the vehicle.
   *
   * @param command The command to be mapped.
   * @return The order request to be sent.
   * @throws IllegalArgumentException If the movement command could not be mapped properly.
   */
  public StateRequest mapToOrder(MovementCommand command)
      throws IllegalArgumentException {
    return null; //new StateRequest(command, null);
  }

  private static int extractDestinationId(Point point)
      throws IllegalArgumentException {
    try {
      return Integer.parseInt(point.getName());
    }
    catch (NumberFormatException e) {
      throw new IllegalArgumentException("Cannot parse point name: " + point.getName(), e);
    }
  }
}
