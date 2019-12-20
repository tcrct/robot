/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.vehicle;

import java.awt.event.ActionListener;

import com.robot.common.telegrams.RequestResponseMatcher;
import com.robot.common.telegrams.StateRequesterTask;
import com.robot.common.telegrams.TelegramSender;
import org.opentcs.data.model.Vehicle;

/**
 * A factory for various instances specific to the example comm adapter.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface RobotAdapterComponentsFactory {

  /**
   * Creates a new RobotCommAdapter for the given vehicle.
   *
   * @param vehicle The vehicle
   * @return A new RobotCommAdapter for the given vehicle
   */
  RobotCommAdapter createExampleCommAdapter(Vehicle vehicle);

  /**
   * Creates a new {@link RequestResponseMatcher}.
   *
   * @param telegramSender Sends telegrams/requests.
   * @return The created {@link RequestResponseMatcher}.
   */
  RequestResponseMatcher createRequestResponseMatcher(TelegramSender telegramSender);

  /**
   * Creates a new {@link StateRequesterTask}.
   *
   * @param stateRequestAction The actual action to be performed to enqueue requests.
   * @return The created {@link StateRequesterTask}.
   */
  StateRequesterTask createStateRequesterTask(ActionListener stateRequestAction);
}
