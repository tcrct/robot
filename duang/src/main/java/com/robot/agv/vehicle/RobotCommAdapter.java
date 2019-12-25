/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle;

import com.google.inject.assistedinject.Assisted;

import static com.robot.agv.common.telegrams.BoundedCounter.UINT16_MAX_VALUE;

import com.robot.agv.common.dispatching.DispatchAction;
import com.robot.agv.common.dispatching.LoadAction;
import com.robot.agv.common.telegrams.*;
import com.robot.agv.utils.SettingUtils;
import com.robot.agv.vehicle.exchange.RobotProcessModelTO;
import com.robot.agv.vehicle.net.ChannelManagerFactory;
import com.robot.agv.vehicle.net.IChannelManager;
import com.robot.agv.vehicle.net.NetChannelType;
import com.robot.agv.vehicle.telegrams.OrderRequest;
import com.robot.agv.vehicle.telegrams.OrderResponse;
import com.robot.agv.vehicle.telegrams.StateRequest;
import com.robot.agv.vehicle.telegrams.StateResponse;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.opentcs.contrib.tcp.netty.ConnectionEventListener;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.util.ExplainedBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An example implementation for a communication adapter.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class RobotCommAdapter
        extends BasicVehicleCommAdapter
        implements ConnectionEventListener<Response>,TelegramSender {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RobotCommAdapter.class);

  /**openTCS的移动命令与发送到所连接车辆的报文映射关系*/
  private final StateMapper stateMapper;

  /**组件工厂*/
  private final RobotAdapterComponentsFactory componentsFactory;

  /**所有请求报文发送的计数器*/
  private final BoundedCounter globalRequestCounter = new BoundedCounter(0, UINT16_MAX_VALUE);
  /**
   * Maps commands to order IDs so we know which command to report as finished.
   */
  private final Map<MovementCommand, String> orderIds = new ConcurrentHashMap<>();
  /**
   * Manages the channel to the vehicle.
   */
  private IChannelManager<Request, Response> channelManager;
  /**
   * 将请求与响应匹配，并保留挂起请求的队列
   */
  private RequestResponseMatcher requestResponseMatcher;
  /**
   * 任务定时器，定期将请求加入队列中
   */
  private StateRequesterTask stateRequesterTask;

  /**
   * 创建适配器
   *
   * @param vehicle 需要连接的车辆
   * @param stateMapper 移动命令的顺序映射器
   * @param componentsFactory 组件工厂
   */
  @Inject
  public RobotCommAdapter(@Assisted Vehicle vehicle,
                          StateMapper stateMapper,
                          RobotAdapterComponentsFactory componentsFactory) {
    super(new RobotProcessModel(vehicle), 3, 2, LoadAction.CHARGE);
    this.stateMapper = requireNonNull(stateMapper, "orderMapper");
    this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
  }

  /**
   * 初始化适配器
   */
  @Override
  public void initialize() {
    super.initialize();
    this.requestResponseMatcher = componentsFactory.createRequestResponseMatcher(this);
//    this.stateRequesterTask = componentsFactory.createStateRequesterTask(e -> {
//      LOG.info("添加新的状态请求到队列");
//      requestResponseMatcher.enqueueRequest(new StateRequest(Telegram.ID_DEFAULT));
//    });

    // 启动定时器
    stateRequesterTask = new StateRequesterTask(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        //
      }
    });
    stateRequesterTask.enable();
    LOG.info("车辆[{}]完成Robot适配器初始化完成", getName());
  }

  /**
   * 终止适配器
   */
  @Override
  public void terminate() {
    stateRequesterTask.disable();
    super.terminate();
    LOG.info("车辆[{}]终止Robot适配器完成", getName());
  }

  /**
   * 启用通讯管理器
   */
  @Override
  public synchronized void enable() {
    if (isEnabled()) {
      LOG.info("车辆[{}]已开启通讯管理器，请勿重复开启", getName());
      return;
    }
    // 创建负责与车辆连接的通讯渠道管理器
    NetChannelType netChannelType = getNetChannelType();
    channelManager = ChannelManagerFactory.getManager(this, netChannelType);
    if (!channelManager.isInitialized()) {
        channelManager.initialize();
    }
    super.enable();
    LOG.info("车辆[{}]通讯管理器启用成功", getName());

    getProcessModel().setVehiclePosition("705");
    getProcessModel().setVehicleIdle(true);
    getProcessModel().setVehicleState(Vehicle.State.IDLE);

  }

  /**
   * 停用通讯渠道管理器
   */
  @Override
  public synchronized void disable() {
    if (!isEnabled()) {
      return;
    }

    super.disable();
    channelManager.terminate();
    channelManager = null;
    LOG.info("车辆[{}]停用通讯成功", getName());
  }

  /**
   * 清除命令队列
   */
  @Override
  public synchronized void clearCommandQueue() {
    super.clearCommandQueue();
    orderIds.clear();//映射关系也要清除
  }

  /**连接车辆*/
  @Override
  protected synchronized void connectVehicle() {
    if (channelManager == null) {
      LOG.warn("{}: 车辆通讯渠道管理器不存在.", getName());
      return;
    }
    // 根据车辆设置的host与port，连接车辆
    channelManager.connect(getHost(), getPort());
  }

  /**断开车辆连接*/
  @Override
  protected synchronized void disconnectVehicle() {
    if (channelManager == null) {
      LOG.warn("{}: 车辆通讯渠道管理器不存在.", getName());
      return;
    }

    channelManager.disconnect();
  }

  /**判断车辆是否已经连接，已经连接返回true*/
  @Override
  protected synchronized boolean isVehicleConnected() {
    return channelManager != null && channelManager.isConnected();
  }

  /***
   * 参数变量
   * @param evt 模型属性发生更改的事件对象
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    super.propertyChange(evt);
    if (!(evt.getSource() instanceof RobotProcessModel)) {
      return;
    }

    // 车辆GUI面板的处理事件
    if (Objects.equals(evt.getPropertyName(),
            VehicleProcessModel.Attribute.COMM_ADAPTER_CONNECTED.name())) {
      if (getProcessModel().isCommAdapterConnected()) {
        // 设置日志记录是否开启
        channelManager.setLoggingEnabled(getProcessModel().isLoggingEnabled());
      }
    }
    if (Objects.equals(evt.getPropertyName(),VehicleProcessModel.Attribute.COMM_ADAPTER_CONNECTED.name()) ||
            Objects.equals(evt.getPropertyName(),RobotProcessModel.Attribute.PERIODIC_STATE_REQUESTS_ENABLED.name())) {
      if (getProcessModel().isCommAdapterConnected()
              && getProcessModel().isPeriodicStateRequestEnabled()) {
        stateRequesterTask.enable();
      }
      else {
        stateRequesterTask.disable();
      }
    }
    if (Objects.equals(evt.getPropertyName(),
            RobotProcessModel.Attribute.PERIOD_STATE_REQUESTS_INTERVAL.name())) {
      stateRequesterTask.setRequestInterval(getProcessModel().getStateRequestInterval());
    }
  }

  @Override
  @Deprecated
  protected List<org.opentcs.drivers.vehicle.VehicleCommAdapterPanel> createAdapterPanels() {
    return new ArrayList<>();
  }

  @Override
  public final RobotProcessModel getProcessModel() {
    return (RobotProcessModel) super.getProcessModel();
  }

  public synchronized void initVehiclePosition(String newPos) {
      getProcessModel().setVehiclePosition(newPos);
  }

  @Override
  protected VehicleProcessModelTO createCustomTransferableProcessModel() {
    // 发送到其他软件（如控制中心或工厂概览）时，添加车辆的附加信息
    return new RobotProcessModelTO()
            .setVehicleRef(getProcessModel().getVehicleReference())
            .setCurrentState(getProcessModel().getCurrentState())
            .setPreviousState(getProcessModel().getPreviousState())
            .setLastOrderSent(getProcessModel().getLastOrderSent())
            .setDisconnectingOnVehicleIdle(getProcessModel().isDisconnectingOnVehicleIdle())
            .setLoggingEnabled(getProcessModel().isLoggingEnabled())
            .setReconnectDelay(getProcessModel().getReconnectDelay())
            .setReconnectingOnConnectionLoss(getProcessModel().isReconnectingOnConnectionLoss())
            .setVehicleHost(getProcessModel().getVehicleHost())
            .setVehicleIdle(getProcessModel().isVehicleIdle())
            .setVehicleIdleTimeout(getProcessModel().getVehicleIdleTimeout())
            .setVehiclePort(getProcessModel().getVehiclePort())
            .setPeriodicStateRequestEnabled(getProcessModel().isPeriodicStateRequestEnabled())
            .setStateRequestInterval(getProcessModel().getStateRequestInterval());
  }

  /**发送移动命令*/
  @Override
  public synchronized void sendCommand(MovementCommand cmd)
          throws IllegalArgumentException {
    requireNonNull(cmd, "cmd");
    LOG.info("cmd: " + cmd);

    try {
//      StateRequest stateRequest = stateMapper.mapToOrder(cmd, getProcessModel().getName());
      StateRequest stateRequest = new StateRequest(cmd, getProcessModel());
      DispatchAction.duang().doAction(stateRequest);
      orderIds.put(cmd, stateRequest.getCode());


      LOG.debug("{}: Enqueuing order telegram with ID {}: {}, {}",
              getName(),
              stateRequest.getCode(),
              stateRequest.getDestinationId(),
              stateRequest.getDestinationAction());
      // 把请求请求加入队列。请求发送规则是FIFO。电报请求将在队列中的第一封电报之后发送。这确保我们总是等待响应，直到发送新请求。
      requestResponseMatcher.enqueueRequest(stateRequest);
      LOG.debug("将车辆[{}]将移动请求提交到消息队列完成", getName());
    }
    catch (IllegalArgumentException exc) {
      LOG.error("车辆[{}]将移动请求命令提交到队列失败: {}", getName(), cmd, exc);
    }
  }

  @Override
  public synchronized ExplainedBoolean canProcess(List<String> operations) {
    requireNonNull(operations, "operations");
    boolean canProcess = true;
    String reason = "";
    if (!isEnabled()) {
      canProcess = false;
      reason = "Adapter not enabled";
    }
    if (canProcess && !isVehicleConnected()) {
      canProcess = false;
      reason = "Vehicle does not seem to be connected";
    }
    if (canProcess
            && getProcessModel().getCurrentState().getLoadState() == StateResponse.LoadState.UNKNOWN) {
      canProcess = false;
      reason = "Vehicle's load state is undefined";
    }
    boolean loaded = getProcessModel().getCurrentState().getLoadState() == StateResponse.LoadState.FULL;
    final Iterator<String> opIter = operations.iterator();
    while (canProcess && opIter.hasNext()) {
      final String nextOp = opIter.next();
      // If we're loaded, we cannot load another piece, but could unload.
      if (loaded) {
        if (nextOp.startsWith(LoadAction.LOAD)) {
          canProcess = false;
          reason = "Cannot load when already loaded";
        }
        else if (nextOp.startsWith(LoadAction.UNLOAD)) {
          loaded = false;
        }
        else if (nextOp.startsWith(DriveOrder.Destination.OP_PARK)) {
          canProcess = false;
          reason = "Vehicle shouldn't park while in a loaded state.";
        }
        else if (nextOp.startsWith(LoadAction.CHARGE)) {
          canProcess = false;
          reason = "Vehicle shouldn't charge while in a loaded state.";
        }
      }
      // If we're not loaded, we could load, but not unload.
      else if (nextOp.startsWith(LoadAction.LOAD)) {
        loaded = true;
      }
      else if (nextOp.startsWith(LoadAction.UNLOAD)) {
        canProcess = false;
        reason = "Cannot unload when not loaded";
      }
    }
    return new ExplainedBoolean(canProcess, reason);
  }

  @Override
  public void processMessage(@Nullable Object message) {
    //Process messages sent from the kernel or a kernel extension
    LOG.info("##############processMessage: {}", message);
  }

  @Override
  public void onConnect() {
    if (!isEnabled()) {
      return;
    }
    getProcessModel().setCommAdapterConnected(true);
    LOG.debug("车辆[{}]连接成功", getName());
    // 检查是否重新发送上一个请求
    requestResponseMatcher.checkForSendingNextRequest();
  }

  @Override
  public void onFailedConnectionAttempt() {
    if (!isEnabled()) {
      return;
    }
    getProcessModel().setCommAdapterConnected(false);
    if (isEnabled() && getProcessModel().isReconnectingOnConnectionLoss()) {
      channelManager.scheduleConnect(getHost(), getPort(), getProcessModel().getReconnectDelay());
    }
  }

  @Override
  public void onDisconnect() {
    LOG.debug("车辆[{}]断开连接成功", getName());
    getProcessModel().setCommAdapterConnected(false);
    getProcessModel().setVehicleIdle(true);
    getProcessModel().setVehicleState(Vehicle.State.UNKNOWN);
    if (isEnabled() && getProcessModel().isReconnectingOnConnectionLoss()) {
      channelManager.scheduleConnect(getHost(), getPort(), getProcessModel().getReconnectDelay());
    }
  }

  @Override
  public void onIdle() {
    LOG.debug("车辆[{}]空闲", getName());
    getProcessModel().setVehicleIdle(true);
    // 如果支持重连则的车辆空间时断开连接
    if (isEnabled() && getProcessModel().isDisconnectingOnVehicleIdle()) {
      LOG.debug("车辆[{}]开启了空闲时断开连接", getName());
      disconnectVehicle();
    }
  }

  /***
   * 接收到报文消息
   * @param response  报文对象
   */
  @Override
  public synchronized void onIncomingTelegram(Response response) {
    requireNonNull(response, "response");

    // 车辆状态设置为不空闲
    getProcessModel().setVehicleIdle(false);

    // 检查响应是否与当前请求匹配，请求与响应应该是一一对应的
    if (!requestResponseMatcher.tryMatchWithCurrentRequest(response)) {
      // 如果不匹配则忽略消息
      return;
    }

    // 如果是状态请求，状态请求是指路径请求，路径状态请求是指车辆移动指令的请求
    if (response instanceof StateResponse) {
      onStateResponse((StateResponse) response);
    }
    // 如果是订单请求，即其它指令请求，例如上报卡号之类的
    else if (response instanceof OrderResponse) {
      LOG.debug("车辆[{}]接收到一个新的订单响应: {}", getName(), response);
    }
    else {
      LOG.warn("车辆[{}]接收到一个未知的报文请求/响应: {}",
              getName(),
              response.getClass().getName());
    }

    //发送下一封电报
    requestResponseMatcher.checkForSendingNextRequest();
  }

  @Override
  public synchronized void sendTelegram(Request telegram) {
    requireNonNull(telegram, "telegram");
    if (!isVehicleConnected()) {
      LOG.debug("{}: Not connected - not sending request '{}'",
              getName(),
              telegram);
      return;
    }

    // Update the request's id
//    telegram.updateRequestContent(globalRequestCounter.getAndIncrement());

    LOG.debug("{}: Sending request '{}'", getName(), telegram);
    channelManager.send(telegram);

    // If the telegram is an order, remember it.
    if (telegram instanceof OrderRequest) {
      getProcessModel().setLastOrderSent((OrderRequest) telegram);
    }

    if (getProcessModel().isPeriodicStateRequestEnabled()) {
      stateRequesterTask.restart();
    }
  }

  public RequestResponseMatcher getRequestResponseMatcher() {
    return requestResponseMatcher;
  }

  private void onStateResponse(StateResponse stateResponse) {
    LOG.info("车辆[{}]接收到一个新的状态响应报文: {}", getName(), stateResponse);

    // 更新车辆的当前状态并记住上一个状态
    getProcessModel().setPreviousState(getProcessModel().getCurrentState());
    getProcessModel().setCurrentState(stateResponse);

    //上一个状态
    StateResponse previousState = getProcessModel().getPreviousState();
    // 当前最新状态
    StateResponse currentState = getProcessModel().getCurrentState();

    // 检查并更新车辆位置
    checkForVehiclePositionUpdate(previousState, currentState);
    // 检查并更新车辆状态
    checkForVehicleStateUpdate(previousState, currentState);
    // 检查订单是否完成
    checkOrderFinished(previousState, currentState);

    // XXX 在此处理从电报中提取的进一步状态更新
  }

  /**
   * 检查并更新车辆位置
   * @param previousState 车辆更新前的状态
   * @param currentState  车辆最新状态
   */
  private void checkForVehiclePositionUpdate(StateResponse previousState, StateResponse currentState) {
    // 如果两个状态的位置是相等的，则退出
    if (previousState.getPositionId() == currentState.getPositionId()) {
        return;
    }
    // 将提交上来的位置更新到调试系统，但位置点数不能为0
    if (currentState.getPositionId() != 0) {
      String currentPosition = String.valueOf(currentState.getPositionId());
      getProcessModel().setVehiclePosition(currentPosition);
      LOG.info("车辆[{}]当前最新位置点: {}", getName(), currentPosition);
    }
  }

  /**
   * 检查并更新车辆状态
   * @param previousState 车辆更新前的状态
   * @param currentState  车辆最新状态
   */
  private void checkForVehicleStateUpdate(StateResponse previousState, StateResponse currentState) {
    // 如果状态没发生变化则退出
    if (previousState.getOperatingState() == currentState.getOperatingState()) {
      return;
    }
    //更新为最新状态
    getProcessModel().setVehicleState(translateVehicleState(currentState.getOperatingState()));
  }

  /**
   * 检查是否为最后订单
   * @param previousState 车辆更新前的状态
   * @param currentState  车辆最新状态
   */
  private void checkOrderFinished(StateResponse previousState, StateResponse currentState) {
    // 如果最后完成的订单ID为空，则退出
    if (currentState.getLastFinishedOrderId() == 0) {
      return;
    }
    // 如果上次完成的订单ID没有发生变化，则退出
    if (previousState.getLastFinishedOrderId() == currentState.getLastFinishedOrderId()) {
      return;
    }
    // 检查新的已完成订单ID是否在已发送订单的队列中。如果是，则将该订单之前的所有订单报告为已完成。
    if (!orderIds.containsValue(currentState.getLastFinishedOrderId())) {
      LOG.debug("{}: Ignored finished order ID {} (reported by vehicle, not found in sent queue).",
              getName(),
              currentState.getLastFinishedOrderId());
      return;
    }

    // 遍历队列，将在这个订单之前的所有移动命令移除
    Iterator<MovementCommand> cmdIter = getSentQueue().iterator();
    boolean finishedAll = false;
    while (!finishedAll && cmdIter.hasNext()) {
      MovementCommand cmd = cmdIter.next();
      cmdIter.remove();
      String orderId = orderIds.remove(cmd);
      if (orderId.equals(currentState.getLastFinishedOrderId())) {
        finishedAll = true;
      }

      LOG.debug("车辆[{}]开始执行id为[{}]的移动命令: {}", getName(), orderId, cmd);
      getProcessModel().commandExecuted(cmd);
    }
  }

  /**
   * 将车辆的运行状态映射到内核的车辆状态
   *
   * @param operationState The vehicle's current operation state.
   */
  private Vehicle.State translateVehicleState(StateResponse.OperatingState operationState) {
    switch (operationState) {
      case IDLE:
        return Vehicle.State.IDLE;
      case MOVING:
      case ACTING:
        return Vehicle.State.EXECUTING;
      case CHARGING:
        return Vehicle.State.CHARGING;
      case ERROR:
        return Vehicle.State.ERROR;
      default:
        return Vehicle.State.UNKNOWN;
    }
  }

  private NetChannelType getNetChannelType() {
    return NetChannelType.valueOf("UDP");
  }

  private String getHost() {
    if (NetChannelType.TCP.equals(getNetChannelType())) {
      return getProcessModel().getVehicleHost();
    }
    else if (NetChannelType.UDP.equals(getNetChannelType())) {
      return SettingUtils.getStringByGroup("host", NetChannelType.UDP.name().toLowerCase(),"0.0.0.0");
    }
    else if (NetChannelType.SERIALPORT.equals(getNetChannelType())) {
      return SettingUtils.getStringByGroup("name", NetChannelType.SERIALPORT.name().toLowerCase(), "COM6");
    }
    return "";
  }

  private Integer getPort() {
    if (NetChannelType.TCP.equals(getNetChannelType())) {
      return getProcessModel().getVehiclePort();
    }
    else if (NetChannelType.UDP.equals(getNetChannelType())) {
      return  SettingUtils.getInt("port", NetChannelType.UDP.name().toLowerCase(),9090);
    }
    else if (NetChannelType.SERIALPORT.equals(getNetChannelType())) {
      return SettingUtils.getInt("baudrate", NetChannelType.SERIALPORT.name().toLowerCase(),38400);
    }
    return 0;
  }

}
