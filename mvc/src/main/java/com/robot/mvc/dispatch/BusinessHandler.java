package com.robot.mvc.dispatch;

import cn.hutool.core.util.ReflectUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.mvc.dispatch.route.Route;
import com.robot.mvc.dispatch.route.RouteHelper;
import com.robot.mvc.exceptions.AgvException;
import com.robot.mvc.utils.ToolsKit;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.Callable;

public class BusinessHandler implements Callable {

    private Request request;
    private Response response;
    private String deviceId;
    private String methodName;

    public BusinessHandler(Request request, Response response) {
        this.request = request;
        this.response = response;
        this.deviceId = request.getProtocol().getDeviceId();
        this.methodName = request.getProtocol().getCommandKey();
    }

    @Override
    public Object call() throws Exception {
        try {
            Route route = Optional.ofNullable(RouteHelper.getRoutes().get(deviceId)).orElseThrow(NullPointerException::new);
            Method method = route.getMethodMap().get(methodName.toLowerCase());
            // 如果Service里没有实现该指令对应的方法，则执行公用的duang方法，直接返回响应协议，防止抛出异常
            if(ToolsKit.isEmpty(method)) {
                method = route.getMethodMap().get("duang");
            }
            Object resultObj = ReflectUtil.invoke(route.getInjectObject(), method, request, response);
            if (response.isResponseTo(request)) {
                response.write(resultObj);
                return response;
            }
        } catch (NullPointerException npe) {
            throw new AgvException("找不到对应的["+deviceId+"]类，请确保类存在: " + npe.getMessage(),npe);
        } catch (Exception e) {
            throw new AgvException(e.getMessage(),e);
        }
    }

}
