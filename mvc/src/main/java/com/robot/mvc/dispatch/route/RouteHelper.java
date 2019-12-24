package com.robot.mvc.dispatch.route;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.robot.mvc.annotations.Service;
import com.robot.mvc.helper.ClassHelper;
import com.robot.mvc.utils.ToolsKit;

import java.lang.reflect.Method;
import java.util.*;

public class RouteHelper {

    private final static Log LOG = LogFactory.get();
    private static Set<String> excludedMethodName = null;
    private static Map<String,Route> ROUTE_MAP = new HashMap<>();

    public static Map<String,Route> getRoutes() {
        if(ROUTE_MAP.isEmpty()) {
            if (null == excludedMethodName) {
                excludedMethodName = ToolsKit.buildExcludedMethodName();
            }
            List<Class<?>> serviceClassList = ClassHelper.duang().getServiceClassList();
            for (Class<?> serviceClass : serviceClassList) {
                Method[] methodArray = serviceClass.getMethods();
                List<Method> methodList = new ArrayList<>();
                for (Method method : methodArray) {
                    if (!ToolsKit.isPublicMethod(method.getModifiers()) ||
                            excludedMethodName.contains(method.getName())) {
                        continue;
                    }
                    methodList.add(method);
                }
                if (ToolsKit.isNotEmpty(methodList)) {
                    Service serviceAnnot = serviceClass.getAnnotation(Service.class);
                    String key = serviceAnnot.value();
                    if (ToolsKit.isEmpty(key)) {
                        int endIndex = serviceClass.getSimpleName().toLowerCase().indexOf("service");
                        if (endIndex > -1) {
                            key = serviceClass.getSimpleName().substring(0, endIndex);
                        }
                    }
                    Map<String, Method> methodMap = new HashMap<>();
                    for (Method method : methodList) {
                        methodMap.put(method.getName().toLowerCase(), method);
                    }
                    ROUTE_MAP.put(key, new Route(serviceClass, methodMap));
                }
            }
            printRouteKey();
        }
        return ROUTE_MAP;
    }

    private static void printRouteKey() {
        List<String> keyList = new ArrayList<>(ROUTE_MAP.keySet());
        if(keyList.isEmpty()) {
            throw new NullPointerException("业务逻辑处理类不存在！");
        }
        Collections.sort(keyList);
        LOG.warn("**************** Route Key ****************");
        for (String key : keyList) {
            Route route = ROUTE_MAP.get(key);
            LOG.info(String.format("route mapping: %s, route: %s", key, route.toString()));
        }
    }
}
