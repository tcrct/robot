package com.robot.agv.common.dispatching;

import cn.hutool.core.util.ReflectUtil;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.agv.utils.ProtocolUtils;
import com.robot.agv.utils.SettingUtils;
import com.robot.agv.vehicle.telegrams.*;
import com.robot.mvc.dispatch.DispatchFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.hutool.core.util.ReflectUtil.invoke;

public class DispatchAction {

    private static final Logger LOG = LoggerFactory.getLogger(DispatchAction.class);
    private static DispatchFactory dispatchFactory = null;
    private static final String methodName = "execute";
    private static DispatchAction dispatchAction;

    private DispatchAction(){
        initDispatchFactory();
    }

    public static synchronized  DispatchAction duang() {
        if (null == dispatchAction) {
            dispatchAction = new DispatchAction();
        }
        return dispatchAction;
    }


    // 执行操作
    public Response doAction(Protocol protocol, TelegramSender telegramSender) {
        // 如果是Order请求(车辆主动上报的)或响应(车辆回复应答)，则直接进行到车辆或设备的Service
        if (OrderRequest.isOrderRequest(protocol) || (OrderResponse.isOrderResponse(protocol))) {
            return (Response) dispatchFactory.execute(new OrderRequest(protocol), telegramSender);
        }
        else if (StateResponse.isStateResponse(protocol)) {
            return (Response)dispatchFactory.execute(new OrderRequest(protocol), telegramSender);
        } else {
            LOG.error("该报文不符合规则：{}", ProtocolUtils.converterString(protocol));
            return null;
        }
    }

    /**
     * 处理车辆移动请求
     * @param request
     */
    public StateResponse doAction(StateRequest request) {
        StateResponse response = (StateResponse)dispatchFactory.execute(request, null);
        return response;
    }

    private void initDispatchFactory() {
        try {
            if(null == dispatchFactory) {
                dispatchFactory =  new DispatchFactory();
            }
        } catch (Exception e) {
            LOG.error("初始化业务处理分发工厂类时出错: {}, {}", e.getMessage(), e);
        }
    }

}
