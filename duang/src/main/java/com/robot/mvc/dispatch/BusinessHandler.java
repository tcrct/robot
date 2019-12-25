package com.robot.mvc.dispatch;

import cn.hutool.core.util.ReflectUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.mvc.dispatch.route.Route;
import com.robot.mvc.dispatch.route.RouteHelper;
import com.robot.mvc.exceptions.AgvException;
import com.robot.mvc.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class BusinessHandler implements Callable {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessHandler.class);

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
            Route route = RouteHelper.getRoutes().get(deviceId);
            if (null == route) {
                LOG.info("与车辆或设备[deviceId]对应的Service没有实现");
                return response;
            }
            Method method = route.getMethodMap().get(methodName.toLowerCase());
            // 如果Service里没有实现该指令对应的方法，则执行公用的duang方法，直接返回响应协议，防止抛出异常
            if (ToolsKit.isEmpty(method)) {
              throw new NullPointerException("["+deviceId+"Service."+methodName+"]没找到");
            }
            Object resultObj = ReflectUtil.invoke(route.getInjectObject(), method, request, response);
            if (response.isResponseTo(request)) {
//                response.write(resultObj);
            }
        } catch (Exception e) {
            throw new AgvException(e.getMessage(), e);
        }
        return response;
    }
}
