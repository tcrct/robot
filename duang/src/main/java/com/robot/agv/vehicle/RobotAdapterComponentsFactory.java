/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle;

import java.awt.event.ActionListener;

import com.robot.agv.common.telegrams.RequestResponseMatcher;
import com.robot.agv.common.telegrams.StateRequesterTask;
import com.robot.agv.common.telegrams.TelegramSender;
import org.opentcs.data.model.Vehicle;

/**
 * Robot通信适配器各种实例的工厂
 *
 * @author Laotang
 */
public interface RobotAdapterComponentsFactory {

  /**
   * 为车辆返回一个指定的适配器
   *
   * @param 车辆
   * @return 为车辆返回一个指定的RobotCommAdapter适配器
   */
  RobotCommAdapter createRobotCommAdapter(Vehicle vehicle);

  /**
   * 创建一个新的 {@link RequestResponseMatcher}.
   *
   * @param telegramSender 发送报文或请求的发送对象
   * @return 创建 {@link RequestResponseMatcher}.
   */
  RequestResponseMatcher createRequestResponseMatcher(TelegramSender telegramSender);

  /**
   * 创建一个新的 {@link StateRequesterTask}.
   *
   * @param stateRequestAction 对准备加入队列的请求执行实际操作的监听器
   * @return The created {@link StateRequesterTask}.
   */
  StateRequesterTask createStateRequesterTask(ActionListener stateRequestAction);
}
