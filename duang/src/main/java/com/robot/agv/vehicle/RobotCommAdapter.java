/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpStatus;
import com.google.inject.assistedinject.Assisted;
import com.robot.agv.common.send.LoadAction;
import com.robot.agv.common.send.SendRequest;
import com.robot.agv.common.telegrams.*;
import com.robot.agv.vehicle.exchange.RobotProcessModelTO;
import com.robot.agv.vehicle.net.ChannelManagerFactory;
import com.robot.agv.vehicle.net.IChannelManager;
import com.robot.agv.vehicle.net.NetChannelType;
import com.robot.agv.vehicle.telegrams.OrderRequest;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.agv.vehicle.telegrams.StateRequest;
import com.robot.agv.vehicle.telegrams.StateResponse;
import com.robot.core.AppContext;
import com.robot.core.handshake.HandshakeTelegram;
import com.robot.core.handshake.RobotTelegramListener;
import com.robot.mvc.exceptions.RobotException;
import com.robot.mvc.helper.ActionHelper;
import com.robot.mvc.interfaces.IAction;
import com.robot.numes.RobotEnum;
import com.robot.utils.RobotUtil;
import com.robot.utils.SettingUtils;
import com.robot.utils.ToolsKit;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.kernel.extensions.controlcenter.vehicles.VehicleEntryPool;
import org.opentcs.util.ExplainedBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.robot.agv.common.telegrams.BoundedCounter.UINT16_MAX_VALUE;
import static java.util.Objects.requireNonNull;

/**
 * 通讯适配器
 * 一车辆一适配器对象，有多少车辆就new多少个RobotCommAdapter对象
 *
 * 所以，在接收报文，发送报文，设备动作完成后需要调用适配器进行操作的，需要找出与车辆对应的适配器
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 * @blame Android Team
 */
public class RobotCommAdapter
        extends BasicVehicleCommAdapter
        implements ConnectionEventListener<Response>, TelegramSender {

    /**
     * This class's logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RobotCommAdapter.class);

    /**
     * openTCS的移动命令与发送到所连接车辆的报文映射关系
     */
    private final StateMapper stateMapper;

    /**
     * 组件工厂
     */
    private final RobotAdapterComponentsFactory componentsFactory;

    /**
     * 所有请求报文发送的计数器
     */
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
    private static StateRequesterTask stateRequesterTask;

    private TCSObjectService objectService;

    /**
     * 自定义动作是否运行
     * 如果key存在，则正在运行该指定的动作组合
     */
    private final static Map<String, String> CUSTOM_ACTIONS_MAP = new java.util.concurrent.ConcurrentHashMap<>();
    private final static Map<String, MovementCommand> LAST_CMD_MAP = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 创建适配器
     *
     * @param vehicle           需要连接的车辆
     * @param stateMapper       移动命令的顺序映射器
     * @param componentsFactory 组件工厂
     */
    @Inject
    public RobotCommAdapter(@Assisted Vehicle vehicle,
                            StateMapper stateMapper,
                            TCSObjectService objectService,
                            VehicleEntryPool vehicleEntryPool,
                            RobotAdapterComponentsFactory componentsFactory) {
        super(new RobotProcessModel(vehicle), 3, 2, LoadAction.CHARGE);
        this.stateMapper = requireNonNull(stateMapper, "orderMapper");
        this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
        this.objectService = requireNonNull(objectService, "objectService");
    }

    /**
     * 取TCSObjectServcie对象
     */
    public TCSObjectService getObjectService() {
        java.util.Objects.requireNonNull(objectService, "objectService is null");
        return objectService;
    }

    /**
     * 取TelegramSender对象
     */
    public TelegramSender getSender() {
        return (TelegramSender) this;
    }

    /**
     * 初始化适配器
     */
    @Override
    public void initialize() {
        super.initialize();
        this.requestResponseMatcher = componentsFactory.createRequestResponseMatcher(this);

        LOG.info("车辆[{}]完成Robot适配器初始化完成", getName());
    }

    /**
     * 终止适配器
     */
    @Override
    public void terminate() {
        if (null != stateRequesterTask) {
            stateRequesterTask.disable();
        }
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
        if (null == channelManager) {
            channelManager = ChannelManagerFactory.getManager(this, netChannelType);
        }
        if (!channelManager.isInitialized()) {
            channelManager.initialize();
        }

        // 启动定时器, 用来发放消息
        if (null == stateRequesterTask) {
            stateRequesterTask = new StateRequesterTask(new RobotTelegramListener(this));
            stateRequesterTask.enable();
            LOG.info("车辆[{}]完成定时器开启", getName());
        }

        LOG.info("车辆[{}]通讯管理器启用成功", getName());



        if ("A006".equals(getName())) {
            getProcessModel().setVehiclePosition("705");
        }
        if ("A010".equals(getName())) {
            getProcessModel().setVehiclePosition("1");
        }

        if ("A009".equals(getName())) {
            getProcessModel().setVehiclePosition("237");
        }

        if ("A033".equals(getName())) {
            getProcessModel().setVehiclePosition("49");
        }
        getProcessModel().setVehicleIdle(true);
        getProcessModel().setVehicleState(Vehicle.State.IDLE);



        super.enable();
        if ("A001".equals(getName())) {
            getProcessModel().setVehiclePosition("218");
//            getProcessModel().setVehiclePosition("213");
//            RobotUtil.initVehicleStatus(getName());
        }

        if ("A002".equals(getName())) {
            getProcessModel().setVehiclePosition("231");
//            RobotUtil.initVehicleStatus(getName());
        }
    }

    /**
     * 停用通讯渠道管理器
     */
    @Override
    public synchronized void disable() {
        if (!isEnabled()) {
            return;
        }
        if (null != channelManager) {
            channelManager.terminate();
            channelManager = null;
        }
        // 清空握手队列
        if (null != HandshakeTelegram.getHandshakeTelegramQueue(getName())) {
            HandshakeTelegram.getHandshakeTelegramQueue(getName()).clear();
        }
        orderIds.clear();
        super.disable();
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

    /**
     * 连接车辆
     */
    @Override
    protected synchronized void connectVehicle() {
        if (channelManager == null) {
            LOG.warn("{}: 车辆通讯渠道管理器不存在.", getName());
            return;
        }
        // 根据车辆设置的host与port，连接车辆
        channelManager.connect(getHost(), getPort());
        LOG.info("连接车辆[{}]成功: [{}]", getName(), (getHost()+":"+getPort()));
    }

    /**
     * 断开车辆连接
     */
    @Override
    protected synchronized void disconnectVehicle() {
        if (channelManager == null) {
            LOG.warn("{}: 车辆通讯渠道管理器不存在.", getName());
            return;
        }

        channelManager.disconnect();
    }

    /**
     * 判断车辆是否已经连接，已经连接返回true
     */
    @Override
    protected boolean isVehicleConnected() {
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
        if (Objects.equals(evt.getPropertyName(), VehicleProcessModel.Attribute.COMM_ADAPTER_CONNECTED.name()) ||
                Objects.equals(evt.getPropertyName(), RobotProcessModel.Attribute.PERIODIC_STATE_REQUESTS_ENABLED.name())) {
            if (getProcessModel().isCommAdapterConnected()
                    && getProcessModel().isPeriodicStateRequestEnabled()) {
                stateRequesterTask.enable();
            } else {
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

    /**
     * 重写父类里的是否可以发送下一条指令
     * <p>
     * 已发送的命令数小于车辆的容量，并且队列中至少有一个命令正在等待发送
     * 并且不是单步模式
     *
     * @return true可以发送
     */
    // 是否需要等待分配，true为需要
    private boolean waitingForAllocation;
    public void setWaitingForAllocation(boolean waitingForAllocation) {
        this.waitingForAllocation = waitingForAllocation;
    }
    @Override
    protected synchronized boolean canSendNextCommand() {
        if (waitingForAllocation) {
            LOG.info("车辆{}需要让行，正等待分配指令", getName());
            requestResponseMatcher.clear();
        }
        return super.canSendNextCommand() && (!getProcessModel().isSingleStepModeEnabled()) && !waitingForAllocation;
//        LOG.info(getName()+ "       " +super.canSendNextCommand()+"               "+(!getProcessModel().isSingleStepModeEnabled()));
//        return true;
    }

    /**
     * 发送移动命令
     */
    @Override
    public synchronized void sendCommand(MovementCommand cmd)
            throws IllegalArgumentException {
        requireNonNull(cmd, "cmd");
        LOG.info("cmd: " + cmd);

//        Block block = objectService.fetchObject(Block.class, "Block-0001");
//        block.getMembers().forEach(new Consumer<TCSResourceReference<?>>() {
//            @Override
//            public void accept(TCSResourceReference<?> tcsResourceReference) {
//                LOG.info("{}", tcsResourceReference.getName());
//            }
//        });

        try {
//      StateRequest stateRequest = stateMapper.mapToOrder(cmd, getProcessModel().getName());
            StateRequest stateRequest = new StateRequest(cmd, getProcessModel());
            //进行业务处理
            StateResponse stateResponse = SendRequest.duang().send(stateRequest, AppContext.getTelegramSender());
            if (stateResponse.getStatus() != HttpStatus.HTTP_OK) {
                LOG.error("车辆[{}]进行业务处理里发生异常，退出处理!", getName());
                return;
            }

            orderIds.put(cmd, cmd.isFinalMovement() ? cmd.getFinalDestination().getName() : cmd.getStep().getDestinationPoint().getName());

            LOG.info("@@@@ {} stateRequest: {}" ,getName(), stateRequest);
            // 把请求请求加入队列。请求发送规则是FIFO。电报请求将在队列中的第一封电报之后发送。这确保我们总是等待响应，直到发送新请求。
            requestResponseMatcher.enqueueRequest(getProcessModel().getName(), stateRequest);
            LOG.debug("将车辆[{}]将移动请求提交到消息队列完成", getName());
        } catch (IllegalArgumentException exc) {
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
                } else if (nextOp.startsWith(LoadAction.UNLOAD)) {
                    loaded = false;
                } else if (nextOp.startsWith(DriveOrder.Destination.OP_PARK)) {
                    canProcess = false;
                    reason = "Vehicle shouldn't park while in a loaded state.";
                } else if (nextOp.startsWith(LoadAction.CHARGE)) {
                    canProcess = false;
                    reason = "Vehicle shouldn't charge while in a loaded state.";
                }
            }
            // If we're not loaded, we could load, but not unload.
            else if (nextOp.startsWith(LoadAction.LOAD)) {
                loaded = true;
            } else if (nextOp.startsWith(LoadAction.UNLOAD)) {
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
        requestResponseMatcher.checkForSendingNextRequest(getProcessModel().getName());
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
     * @param response  接收到的报文对象
     */
    @Override
    public synchronized void onIncomingTelegram(Response response) {
        requireNonNull(response, "response");

        // 车辆状态设置为不空闲
        getProcessModel().setVehicleIdle(false);
        StateResponse stateResponse = (StateResponse) response;

        // 检查响应是否与当前请求匹配，请求与响应应该是一一对应的
        if (!requestResponseMatcher.tryMatchWithCurrentRequest(stateResponse)) {
            // 如果不匹配则忽略消息
            LOG.error("该报文不存在系统的队列中或需要作预停车处理，忽略该报文退出");
            return;
        }

        // 如果是状态请求，状态请求是指路径请求，路径状态请求是指车辆移动指令的请求
        if (response instanceof StateResponse) {
            onStateResponse((StateResponse) response);
        }
        // 如果是订单请求，即其它指令请求，例如上报卡号之类的
//    else if (response instanceof OrderResponse) {
//      LOG.debug("车辆[{}]接收到一个新的订单响应: {}", getName(), response.getRawContent());
//    }
        else {
            LOG.warn("车辆[{}]接收到一个未知的报文请求/响应: {}",
                    getName(),
                    response.getClass().getName());
        }

        //如果不需要等待分配则立即发送下一封电报
        if(!waitingForAllocation) {
            requestResponseMatcher.checkForSendingNextRequest(getProcessModel().getName());
        }
    }

    @Override
    public void sendTelegram(Request telegram) {
        requireNonNull(telegram, "telegram");
        if (!isVehicleConnected()) {
            LOG.debug("{}: Not connected - not sending request '{}'",
                    getName(),
                    telegram);
            return;
        }

        // Update the request's id
//    telegram.updateRequestContent(globalRequestCounter.getAndIncrement());

        Protocol protocol = telegram.getProtocol();

        if (RobotEnum.UP_LINK.getValue().equals(protocol.getDirection()) &&
                protocol.getCommandKey().startsWith("rpt")) {
            LOG.info("报文内容[{}]的指令为[{}]，属于等待回复请求，退出发送！", telegram.getRawContent(), protocol.getCommandKey());
            return;
        }

        String deviceId = protocol.getDeviceId();
        if (NetChannelType.SERIALPORT.equals(AppContext.getNetChannelType())) {
            telegram.addSerialPortToRwaContent(deviceId);
        }

        LOG.info("发送报文内容[{}]，到车辆/设备[{}/{}]", telegram.getRawContent(), deviceId, getName());
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
        LOG.info("车辆[{}]接收到一个新的状态响应报文: {}", getName(), stateResponse.getRawContent());
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
     *
     * @param previousState 车辆更新前的状态
     * @param currentState  车辆最新状态
     */
    private void checkForVehiclePositionUpdate(StateResponse previousState, StateResponse currentState) {
        // 如果两个状态的位置是相等的，则退出
        if (currentState.getPositionId().equals(previousState.getPositionId())) {
            return;
        }
        String currentPosition = currentState.getPositionId();
        // 将提交上来的位置更新到调试系统，但位置点数不能为0
        if (ToolsKit.isNotEmpty(currentPosition)) {
            getProcessModel().setVehiclePosition(currentPosition);
            LOG.info("车辆[{}]当前最新位置点: {}", getName(), currentPosition);
        }
    }

    /**
     * 检查并更新车辆状态
     *
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
     *
     * @param previousState 车辆更新前的状态
     * @param currentState  车辆最新状态
     */
    private void checkOrderFinished(StateResponse previousState, StateResponse currentState) {
        // 如果最后完成的订单ID为空，则退出
        if (ToolsKit.isEmpty(currentState.getLastFinishedOrderId())) {
            return;
        }
        // 如果上次完成的订单ID没有发生变化，则退出
        if (currentState.getLastFinishedOrderId().equals(previousState.getLastFinishedOrderId())) {
            return;
        }
        // 检查新的已完成订单ID是否在已发送订单的队列中。如果是，则将该订单之前的所有订单报告为已完成。
        if (!orderIds.containsValue(currentState.getLastFinishedOrderId())) {
            LOG.info("{}: Ignored finished order ID {} (reported by vehicle, not found in sent queue).",
                    getName(),
                    currentState.getLastFinishedOrderId());
            return;
        }
        MovementCommand cmd = getSentQueue().peek();
        // 不是NOP，是最后一条指令并且自定义动作组合里包含该动作名称
        if (!cmd.isWithoutOperation() &&
                cmd.isFinalMovement() &&
                RobotUtil.isContainActionsKey(cmd)) {
            // 如果动作指令操作未运行则可以运行
            String operation = cmd.getOperation();
            if (!CUSTOM_ACTIONS_MAP.containsKey(operation)) {
                LOG.info("车辆[" + getName() + "]对应位置[" + cmd.getStep().getSourcePoint().getName() + "]上的设备开始执行动作[" + operation + "]");
                executeCustomCmds(cmd, getName(), operation);
            } else {
                LOG.info("不能重复执行该操作，因该动作指令已经运行，作丢弃处理！");
            }
        } else {
            LOG.info("车辆[{}]开始移动到点为[{}]的移动命令: {}", getName(), cmd.getStep().getDestinationPoint().getName(), cmd);
            MovementCommand curCommand = getSentQueue().poll();
            if (cmd != null && cmd.equals(curCommand)) {
                getProcessModel().commandExecuted(cmd);
            }
        }

        /*
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
            // 不是NOP，是最后一条指令并且自定义动作组合里包含该动作名称
            if (!cmd.isWithoutOperation() &&
                    cmd.isFinalMovement() &&
                    RobotUtil.isContainActionsKey(cmd)) {
                // 如果动作指令操作未运行则可以运行
                String operation = cmd.getOperation();
                if (!CUSTOM_ACTIONS_MAP.containsKey(operation)) {
                    LOG.info("车辆[" + getName() + "]对应位置[" + cmd.getStep().getSourcePoint().getName() + "]上的设备开始执行动作[" + operation + "]");
                    executeCustomCmds(cmd, getName(), operation);
                } else {
                    LOG.info("不能重复执行该操作，因该动作指令已经运行，作丢弃处理！");
                }
            } else {
                LOG.debug("车辆[{}]开始移动到点为[{}]的移动命令: {}", getName(), orderId, cmd);
//        executeNextMoveCmd(getName(), "");
                getProcessModel().commandExecuted(cmd);
            }
//
        }
         */
    }

    /**
     * 执行自定义指令组合
     *
     * @param operations 指令组合标识字符串
     */
    private void executeCustomCmds(MovementCommand cmd, String deviceId, String operations) {
        final String operation = requireNonNull(operations, "需要执行的动作名称不能为空");
        if (!isEnabled()) {
            LOG.error("适配器没开启，请先开启！");
            return;
        }
        LOG.info("车辆[{}]开始执行自定义指令集合[{}]操作", deviceId, operation);
        try {
            //设置为执行状态
            getProcessModel().setVehicleState(Vehicle.State.EXECUTING);
            // 设置为允许单步执行，即等待自定义命令执行完成或某一指令取消单步操作模式后，再发送移动车辆命令。
            getProcessModel().setSingleStepModeEnabled(true);
            // 线程执行自定义指令队列
            ThreadUtil.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        IAction action = ActionHelper.duang().getCustomActionsQueue().get(operation);
                        if (ToolsKit.isNotEmpty(action)) {
                            action.execute();
                        } else {
                            LOG.info("根据[{}]查找不到对应的动作指令处理类", operation);
                        }
                    } catch (Exception e) {
                        LOG.error("执行自定义动作组合指令时出错: " + e.getMessage(), e);
                    }
                }
            });
//      // 如果工作站执行时间超过10分钟，则放弃等待
//      Boolean isExecuteSuccess = (Boolean) futureTask.get();
////      // 成功执行完，返回true
//      if (isExecuteSuccess) {
//        executeNextMoveCmd(deviceId, operation);
//      }

//      LAST_CMD_MAP.put(deviceId, cmd);
            CUSTOM_ACTIONS_MAP.put(operation, operation);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * 执行下一步移动车辆
     */
    public void executeNextMoveCmd(String deviceId, String actionKey) {
        LOG.info("成功执行自定义指令完成，则检查是否有下一订单，如有则继续执行");
        RobotProcessModel processModel = getProcessModel();
        //车辆设置为空闲状态，执行下一个移动指令
        processModel.setVehicleState(Vehicle.State.IDLE);
        // 取消单步执行状态
        processModel.setSingleStepModeEnabled(false);
//        MovementCommand cmd1 = LAST_CMD_MAP.get(deviceId); //getSentQueue().poll();
//        System.out.println("cmd1: " + cmd1);
        MovementCommand cmd = getSentQueue().poll();
//        System.out.println("cmd.getStep().getSourcePoint(): " + cmd.getStep().getSourcePoint());
        System.out.println("cmd: " + cmd);
        //移除指定动作的名称
        if (ToolsKit.isNotEmpty(actionKey)) {
            CUSTOM_ACTIONS_MAP.remove(actionKey);
        }
        LOG.info("#################deviceId: " + deviceId + "                    getName: " + getName());
        if (!deviceId.equals(getName())) {
            throw new RobotException("车辆标识["+deviceId+"]与对应的适配器["+getName()+"]不匹配");
        }
//    LAST_CMD_MAP.remove(deviceId);
        if (null != cmd) {
            processModel.commandExecuted(cmd);
        }
    }
//    public void executeNextMoveCmd(String deviceId, String actionKey) {
//        LOG.info("成功执行自定义指令完成，则检查是否有下一订单，如有则继续执行");
//        RobotCommAdapter adapter = AppContext.getCommAdapter(deviceId);
//        RobotProcessModel processModel = adapter.getProcessModel();
//        //车辆设置为空闲状态，执行下一个移动指令
//        processModel.setVehicleState(Vehicle.State.IDLE);
//        // 取消单步执行状态
//        processModel.setSingleStepModeEnabled(false);
////        MovementCommand cmd1 = LAST_CMD_MAP.get(deviceId); //getSentQueue().poll();
////        System.out.println("cmd1: " + cmd1);
//        MovementCommand cmd = adapter.getSentQueue().poll();
////        System.out.println("cmd.getStep().getSourcePoint(): " + cmd.getStep().getSourcePoint());
//        System.out.println("cmd: " + cmd);
//        //移除指定动作的名称
//        if (ToolsKit.isNotEmpty(actionKey)) {
//            CUSTOM_ACTIONS_MAP.remove(actionKey);
//        }
//        LOG.info("#################deviceId: " + deviceId + "                    getName: " + processModel.getName());
////    LAST_CMD_MAP.remove(deviceId);
//        if (null != cmd) {
//            processModel.commandExecuted(cmd);
//        }
//    }

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
        NetChannelType type = AppContext.getNetChannelType();
        if (ToolsKit.isEmpty(type)) {
            type = NetChannelType.UDP;
        }
        return type;
    }

    private String getHost() {
        if (NetChannelType.TCP.equals(getNetChannelType())) {
            return getProcessModel().getVehicleHost();
        } else if (NetChannelType.UDP.equals(getNetChannelType())) {
//            return SettingUtils.getStringByGroup("host", NetChannelType.UDP.name().toLowerCase(), "0.0.0.0");
            return  getProcessModel().getVehicleHost();
        } else if (NetChannelType.SERIALPORT.equals(getNetChannelType())) {
            return SettingUtils.getStringByGroup("name", NetChannelType.SERIALPORT.name().toLowerCase(), "COM6");
        }
        return "";
    }

    private Integer getPort() {
        if (NetChannelType.TCP.equals(getNetChannelType())) {
            return getProcessModel().getVehiclePort();
        } else if (NetChannelType.UDP.equals(getNetChannelType())) {
//            return SettingUtils.getInt("port", NetChannelType.UDP.name().toLowerCase(), 9090);
            return getProcessModel().getVehiclePort();
        } else if (NetChannelType.SERIALPORT.equals(getNetChannelType())) {
            return SettingUtils.getInt("baudrate", NetChannelType.SERIALPORT.name().toLowerCase(), 38400);
        }
        return 0;
    }
}


