package com.robot.agv.common.send;

import com.robot.agv.common.telegrams.Response;
import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.service.common.ActionRequest;
import com.robot.service.common.ActionResponse;
import com.robot.utils.ProtocolUtils;
import com.robot.agv.vehicle.telegrams.*;
import com.robot.mvc.dispatch.DispatchFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendRequest {

    private static final Logger LOG = LoggerFactory.getLogger(SendRequest.class);
    private static DispatchFactory dispatchFactory = null;
    private static final String methodName = "execute";
    private static SendRequest sendRequest;

    private SendRequest(){
        initDispatchFactory();
    }

    public static SendRequest duang() {
        if (null == sendRequest) {
            sendRequest = new SendRequest();
        }
        return sendRequest;
    }


    // TCP、UDP、串口接收到报文信息
    public Response send(Protocol protocol, TelegramSender telegramSender)  {
        // 如果是Order请求(车辆主动上报的)或响应(车辆回复应答)，则直接进行到车辆或设备的Service
        if (ProtocolUtils.isOrderRequest(protocol) || (ProtocolUtils.isOrderResponse(protocol))) {
            return (Response) dispatchFactory.execute(new OrderRequest(protocol), telegramSender);
        }
        else if (ProtocolUtils.isStateProtocol(protocol.getCommandKey())) {
            return (Response)dispatchFactory.execute(new StateRequest(protocol), telegramSender);
        } else {
            LOG.error("该报文不符合规则：{}", ProtocolUtils.converterString(protocol));
            return null;
        }
    }

    /**
     * 处理车辆移动请求
     * @param request
     */
    public StateResponse send(StateRequest request, TelegramSender telegramSender) {
        StateResponse response = (StateResponse)dispatchFactory.execute(request, telegramSender);
        return response;
    }

    /**
     * 设备动作指令请求
     * @param request
     * @param telegramSender
     * @return
     */
    public ActionResponse send(ActionRequest request, TelegramSender telegramSender) {
        ActionResponse response = (ActionResponse)dispatchFactory.execute(request, telegramSender);
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
