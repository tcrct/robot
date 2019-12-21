/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.common.telegrams;

import com.google.inject.assistedinject.Assisted;
import java.awt.event.ActionListener;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.swing.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务定时器，定期将请求报文加入队列
 *
 * @author Laotang
 */
public class StateRequesterTask {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StateRequesterTask.class);
  /**
   * The actual action to be performed to enqueue requests.
   */
  private final ActionListener stateRequestAction;
  /**
   * A timer for enqueuing requests.
   */
  private Timer stateRequestTimer;
  /**
   * The interval requests should be enqueued.
   */
  private int requestInterval = 500;

  /**
   * Creates a new instance.
   *
   * @param stateRequestAction The actual action to be performed to enqueue requests.
   */
  @Inject
  public StateRequesterTask(@Nonnull @Assisted ActionListener stateRequestAction) {
    this.stateRequestAction = requireNonNull(stateRequestAction, "stateRequestAction");
  }

  public void enable() {
    if (stateRequestTimer != null) {
      return;
    }
    LOG.debug("Starting state requester task.");
    stateRequestTimer = new Timer(requestInterval, stateRequestAction);
    stateRequestTimer.start();
  }

  public void disable() {
    if (stateRequestTimer == null) {
      return;
    }
    LOG.debug("Stopping state requester task.");
    stateRequestTimer.stop();
    stateRequestTimer = null;
  }

  /**
   * Restarts the timer for enqueuing new requests.
   */
  public void restart() {
    if (stateRequestTimer == null) {
      LOG.debug("Not enabled, doing nothing.");
      return;
    }
    stateRequestTimer.restart();
  }

  /**
   * 设置任务队列的间隔时间
   *
   * @param requestInterval  间隔时间值，毫秒作单位
   */
  public void setRequestInterval(int requestInterval) {
    this.requestInterval = requestInterval;
  }
}
