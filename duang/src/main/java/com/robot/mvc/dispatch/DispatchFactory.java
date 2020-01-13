package com.robot.mvc.dispatch;

import cn.hutool.core.thread.ThreadUtil;

import cn.hutool.http.HttpStatus;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.agv.vehicle.telegrams.*;
import com.robot.core.AppContext;
import com.robot.core.Sensor;
import com.robot.core.handshake.HandshakeTelegram;
import com.robot.entity.Logs;
import com.robot.mvc.dispatch.route.Route;
import com.robot.mvc.dispatch.route.RouteHelper;
import com.robot.mvc.exceptions.RobotException;
import com.robot.numes.RobotEnum;
import com.robot.service.common.ActionRequest;
import com.robot.service.common.ActionResponse;
import com.robot.utils.DbKit;
import com.robot.utils.ProtocolUtils;
import com.robot.utils.RobotUtil;
import com.robot.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 调度分发工厂
 * 根据协议指令中的车辆ID及指令动作，将协议分发到对应的service里的method。
 * 所以在Service里必须要实现对应指令动作的方法。
 *
 * @author Laotang
 * @blame Android Team
 */
public class DispatchFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DispatchFactory.class);

    private static final Map<String, Route> SERVICE_METHOD_MAP = new ConcurrentHashMap<>();


//    public Object execute(StateRequest stateRequest) {
//        if (SERVICE_METHOD_MAP.isEmpty()) {
//            SERVICE_METHOD_MAP.putAll(RouteHelper.getRoutes());
//        }
//        FutureTask<Response> futureTask = (FutureTask<Response>) ThreadUtil.execAsync(new BusinessHandler(stateRequest, response));
//    }

    /**
     * 根据IProtocol里的参数，反射调用对应Service里的方法
     * @param request
     * @param sender
     */
    public Object execute(Request request, TelegramSender sender) {
        if (SERVICE_METHOD_MAP.isEmpty()) {
            SERVICE_METHOD_MAP.putAll(RouteHelper.duang().getRoutes());
        }

        // 返回对象
        Response response = null;
        final Protocol protocol = request.getProtocol();
        // 保存到数据库
        ThreadUtil.execAsync(new Runnable() {
            @Override
            public void run() {
                // 将所有rpt开头的协议缓存起来，因为rpt*指令上报在某些场景下可能会比动作指令快，导致动作指令一直在等待上报
//                if (protocol.getCommandKey().startsWith("rpt")) {
//                    AppContext.getAdvanceReportMap().put(protocol.getCode(), protocol);
//                }
                //将所有接收到的报文保存到数据库
                DbKit.duang().saveLogs(new Logs(protocol));
            }
        });

        // 如果是订单请求，车辆主动上报的请求
        if (request instanceof OrderRequest) {
            if (ToolsKit.isEmpty(protocol)) {
                throw new RobotException("非移动车辆请求的协议对象不能为空，返回null退出处理！");
            }
            response = new OrderResponse(request);
        }
        //如果是移动请求,发送移动命令
        else if (request instanceof StateRequest) {
            response= new StateResponse(request);
        }
        // 如果是动作请求，发送设备动作
        else if (request instanceof  ActionRequest) {
            response= new ActionResponse(request) {
                @Override
                public String cmd() {
                    return request.getProtocol().getCommandKey();
                }
            };
        }

        // 如果协议对象不为空且不是调度系统主动发送的，则要进行应答回复
        if (ToolsKit.isNotEmpty(protocol) && !request.isRobotSend()) {
            String direction = protocol.getDirection();
            final String deviceId = protocol.getDeviceId();
            String code = protocol.getCode();
            String cmdKey = protocol.getCommandKey();
            // 如果是车辆/设备主动发送的请求则进行应答回复
            if (RobotEnum.UP_LINK.getValue().equalsIgnoreCase(direction)) {
                ThreadUtil.execute(new AnswerHandler(protocol, sender));
            }
            // 如果是r方向或者是rpt的请求指令，则将握手队列中对应的元素移除，停止重复发送
            if (RobotEnum.DOWN_LINK.getValue().equals(direction) ||
                    (RobotEnum.UP_LINK.getValue().equals(direction) && cmdKey.startsWith("rpt"))) {
                // 如果是rptmt指令
                if ("rptmt".equalsIgnoreCase(cmdKey)) {
//                    if (RobotEnum.UP_LINK.getValue().equals(direction)) {
//                        RobotUtil.addSensorStatus(protocol);
//                    }
                    Sensor sensor = Sensor.getSensor(deviceId);
                    if (ToolsKit.isNotEmpty(sensor) && sensor.isWith(protocol.getParams())) {
                        // 取出传感器里的code
                        code = sensor.getCode();
                        LOG.info("车辆/设备[{}]传感器验证参数code为[{}]", deviceId, code);
                    }
                }
                final String removeCode= code;
                // 响应上报的(r)，需要将握手列队中对应的消息移除(如果存在)
                ThreadUtil.execAsync(new Runnable() {
                    @Override
                    public void run() {
//                        AppContext.getAdvanceReportMap().remove(removeCode);
                        HandshakeTelegram.duang().remove(deviceId, removeCode);
                    }
                });
                // rptac指令放行
                if (!"rptac".equalsIgnoreCase(cmdKey)) {
                    return response;
                }
            }
        }

        // 如果是调度系统发起的请求并且是ActionRequest请求，暂不进行业务逻辑处理，直接发送指令返回
        if (request.isRobotSend() && (request instanceof ActionRequest)) {
            sender.sendTelegram(request);
            // 计算设备/车辆响应的协议内容及验证码，存放在response里返回，让日志显示
            return RobotUtil.simulation(response);
        }

        // 线程进行业务处理
        FutureTask<Response> futureTask = (FutureTask<Response>) ThreadUtil.execAsync(new BusinessHandler(request, response));
        try {
            response =  futureTask.get(3000L, TimeUnit.MILLISECONDS);
            if ((response.getStatus() == HttpStatus.HTTP_OK) && response.isResponseTo(request)) {
                // 将返回内容更新到request
                request.updateRequestContent(response);
                // 如果sender不为null且不是StateRequest请求，且不是上报卡号的报文
                // 则直接发送报文到客户端
                if (null != sender && !(request instanceof StateRequest)
                        && !ProtocolUtils.isStateProtocol(protocol.getCommandKey())) {
                    // 保存到数据库
                    ThreadUtil.execAsync(new Runnable() {
                        @Override
                        public void run() {
                            //将所有接收到的报文保存到数据库
                            DbKit.duang().saveLogs(new Logs(protocol));
                        }
                    });
                    sender.sendTelegram(request);
                }
            }
        } catch (InterruptedException ie) {
            LOG.error("执行时发生InterruptedException: {}, {}", ie.getMessage(), ie);
            response.setStatus(HttpStatus.HTTP_NOT_FOUND);
        } catch (ExecutionException ee) {
            LOG.error("执行时发生ExecutionException :{}, {}", ee.getMessage(), ee);
            response.setStatus(HttpStatus.HTTP_INTERNAL_ERROR);
        } catch (TimeoutException te) {
            LOG.error("执行时发生TimeoutException:{}, {}", te.getMessage(), te);
            response.setStatus(HttpStatus.HTTP_CLIENT_TIMEOUT);
        } finally {
            if (futureTask.isDone()) {
                // 中止线程，参数为true时，会中止正在运行的线程，为false时，如果线程未开始，则停止运行
                futureTask.cancel(true);
            }
        }
        return response;
    }

}
