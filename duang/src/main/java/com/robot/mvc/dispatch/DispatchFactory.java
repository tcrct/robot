package com.robot.mvc.dispatch;

import cn.hutool.core.thread.ThreadUtil;

import cn.hutool.http.HttpStatus;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.agv.vehicle.telegrams.*;
import com.robot.core.AppContext;
import com.robot.core.handshake.HandshakeTelegram;
import com.robot.core.handshake.HandshakeTelegramDto;
import com.robot.mvc.dispatch.route.Route;
import com.robot.mvc.dispatch.route.RouteHelper;
import com.robot.mvc.exceptions.RobotException;
import com.robot.numes.RobotEnum;
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
     */
    public Object execute(Request request, TelegramSender sender) {
        if (SERVICE_METHOD_MAP.isEmpty()) {
            SERVICE_METHOD_MAP.putAll(RouteHelper.getRoutes());
        }

        // 返回对象
        Response response = null;
        Protocol protocol = request.getProtocol();
        if (request instanceof OrderRequest) {
            if (ToolsKit.isEmpty(protocol)) {
                throw new RobotException("非移动车辆请求的协议对象不能为空，返回null退出处理！");
            }
            response = new OrderResponse(request);
        } else {
            response= new StateResponse(request);
        }


        if (ToolsKit.isNotEmpty(protocol)) {
            // 线程进行应答回复
            ThreadUtil.execute(new AnswerHandler(protocol, sender));
            // 如果是r方向，则不需要进行到Service
            if (RobotEnum.DOWN_LINK.getValue().equals(protocol.getDirection())) {
                // 响应上报的(r)，需要将握手列队中对应的消息移除(如果存在)
                HandshakeTelegram.duang().remove(protocol.getDeviceId(), protocol.getCode());
                return response;
            }
        }

        // 线程进行业务处理
        FutureTask<Response> futureTask = (FutureTask<Response>) ThreadUtil.execAsync(new BusinessHandler(request, response));
        try {
            response =  futureTask.get(3000L, TimeUnit.MILLISECONDS);
            if (response.isResponseTo(request)) {
                // 将返回内容更新到request
                request.updateRequestContent(response);
                // 如果sender不为null且不是StateRequest请求，且不是上报卡号的报文
                // 则直接发送报文到客户端
                if (null != sender && !(request instanceof StateRequest)
                        && !ProtocolUtils.isStateProtocol(protocol.getCommandKey())) {
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
