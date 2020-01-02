package com.robot.core.handshake;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.telegrams.OrderRequest;
import com.robot.agv.vehicle.telegrams.OrderResponse;
import com.robot.agv.vehicle.telegrams.StateRequest;
import com.robot.agv.vehicle.telegrams.StateResponse;
import com.robot.mvc.interfaces.ICallback;
import com.robot.utils.ToolsKit;
import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 * 存放在握手对队里的报文对象Dto
 *
 * @author Laotang
 */
public class HandshakeTelegramDto implements Serializable {

    private static final Logger logger = Logger.getLogger(HandshakeTelegramDto.class);

    /**请求对象*/
    private Request request;
    /**返回对象*/
    private Response response;
    /**注册回调事件*/
    private ICallback callback;
    /**指令动作列表名称*/
    private String actionKey;

    public HandshakeTelegramDto(HandshakeTelegramDto dto) {
        this(dto.getRequest(), dto.getResponse(),dto.getCallback(), dto.getActionKey());
    }

    public HandshakeTelegramDto(Request request, Response response) {
        this.request = request;
        this.response = response;
    }

    public HandshakeTelegramDto(Request request, Response response, ICallback callback, String actionKey) {
        this.request = request;
        this.response = response;
        this.callback = callback;
        this.actionKey = actionKey;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public ICallback getCallback() {
        return callback;
    }

    public void setCallback(ICallback callback) {
        this.callback = callback;
    }

    public String getActionKey() {
        return actionKey;
    }

    public void setActionKey(String actionKey) {
        this.actionKey = actionKey;
    }

    @Override
    public String toString() {
        if(ToolsKit.isEmpty(response)) {
            logger.error("HandshakeTelegramDto response is null");
            return "";
        }
        return "HandshakeTelegramDto{" +
                ", telegram=" + response.getRawContent() +
                '}';
    }
}
