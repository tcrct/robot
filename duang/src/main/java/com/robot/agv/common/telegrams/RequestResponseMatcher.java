/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.common.telegrams;

import com.google.inject.assistedinject.Assisted;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.mongodb.lang.NonNull;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.agv.vehicle.telegrams.StateRequest;
import com.robot.agv.vehicle.telegrams.StateResponse;
import com.robot.core.AppContext;
import com.robot.core.handshake.HandshakeTelegram;
import com.robot.core.handshake.HandshakeTelegramDto;
import com.robot.numes.RobotEnum;
import com.robot.utils.ProtocolUtils;
import com.robot.utils.RobotUtil;
import com.robot.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps {@link Request}s in a queue and matches them with incoming {@link Response}s.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RequestResponseMatcher {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RequestResponseMatcher.class);
  /**
   * The actual queue of requests.
   */
  private final Map<String, LinkedList<Request>> requests = new ConcurrentHashMap<>();
  /**
   * Sends the queued {@link Request}s.
   */
  private final TelegramSender telegramSender;

  /**
   * Creates a new instance.
   *
   * @param telegramSender Sends the queued {@link Request}s.
   */
  @Inject
  public RequestResponseMatcher(@Assisted TelegramSender telegramSender) {
    this.telegramSender = requireNonNull(telegramSender, "telegramSender");
  }

  /**
   * 将请求对象加入到队列中
   * @param request 请求对象
   */
  public void enqueueRequest(@NonNull String deviceId, @Nonnull Request request) {
    requireNonNull(request, "请求对象不能为空");
    boolean emptyQueueBeforeEnqueue = requests.isEmpty();

    LOG.info("加入到车辆移动队列的请求: {}", request.getRawContent());
    LinkedList<Request> linkedList = requests.get(deviceId);
    if (ToolsKit.isEmpty(linkedList)) {
      linkedList = new LinkedList<>();
    }
    linkedList.add(request);
    requests.put(deviceId, linkedList);

    if (emptyQueueBeforeEnqueue) {
      checkForSendingNextRequest(deviceId);
    }
  }

  /**
   * Checks if a telegram is enqueued and sends it.
   */
  public void checkForSendingNextRequest(String deviceId) {
    LOG.info("Check for sending next request.");
    if (peekCurrentRequest(deviceId).isPresent()) {
      Request request = peekCurrentRequest(deviceId).get();
      telegramSender.sendTelegram(peekCurrentRequest(deviceId).get());
      // 添加到应答(握手)队列
      if (AppContext.isHandshakeListener() &&
              RobotEnum.UP_LINK.getValue().equals(request.getProtocol().getDirection())) {
        LOG.info("添加到握手队列中的报文: {}", request.getRawContent());
        HandshakeTelegram.duang().add(new HandshakeTelegramDto(new StateResponse(request)));
      }
    }
    else {
      LOG.info("No requests to be sent.");
    }
  }

  /**
   * 根据指定的设备ID取Request对象
   * @param deviceId 设备ID
   */
  public Optional<Request> peekCurrentRequest(String deviceId) {
    Queue queue = requests.get(deviceId);
    return ToolsKit.isEmpty(queue) ? Optional.empty() : Optional.of((Request) queue.peek());
  }

  /**
   * Returns <code>true</code> if the response matches to the first request in the queue.
   * If it matches, the request will be removed.
   *
   * @param response The response to match
   * @return <code>true</code> if the response matches to the first request in the queue.
   */
  public boolean tryMatchWithCurrentRequest(@Nonnull StateResponse response) {
    requireNonNull(response, "response");

    Protocol protocol = response.getProtocol();
    if (ToolsKit.isEmpty(protocol) || ToolsKit.isEmpty(protocol.getCommandKey())) {
      LOG.info("请求匹配时，协议对象或指令名称不能为空");
      return false;
    }

    String cmdKey = protocol.getCommandKey();
    // 如果不是报告卡号的指令则退出
    if (!(ProtocolUtils.isRptacProtocol(cmdKey) || ProtocolUtils.isRptrtpProtocol(cmdKey))) {
      LOG.info("不是报告卡号的指令，退出处理");
      return false;
    }

    String deviceId = protocol.getDeviceId();
    LinkedList<Request> queue = requests.get(deviceId);
    if (ToolsKit.isEmpty(queue)) {
      LOG.info("根据[{}]查找不到对应的移动命令队列", deviceId);
      return false;
    }
    StateRequest currentRequest = (StateRequest)queue.peek();
    // 如果最后一个指令是预停车的(s)，则需要判断参数是否以1结尾
    if (currentRequest.isPreStop()) {
      if (ProtocolUtils.isRptrtpProtocol(cmdKey)) {
        if (!protocol.getParams().endsWith("1")) {
          LOG.info("车辆 [" + deviceId + "]预停车不成功，[" + protocol.getParams() + "]最后一位参数不为1");
          return false;
        }
        LOG.info("车辆 [" + deviceId + "]预停车成功");
      } else {
        LOG.info("车辆 [" + deviceId + "]最后一个指令是预停车，等待预停车到位指令[{}]上报再作处理", "rptrtp");
        return false;
      }
    }

    // 如果上报的卡号与队列中的第1位是一致的
    if (currentRequest != null && response.containsPoint(currentRequest)) {
      queue.remove();
      return true;
    }

    if (currentRequest != null) {
      LOG.info("匹配不成功，请求的报文的点号与系统队列中第1位报文的点号不一致或该点不存在队列中");
    }
    else {
      LOG.info("接收到的响应报文[{}], 但没有请求等待响应", response.getRawContent());
    }

    return false;
  }

  /**
   * Clears all requests stored in the queue.
   */
  public void clear() {
    requests.clear();
  }
}
