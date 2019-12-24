package com.robot.mvc.helper;


import com.robot.mvc.annotations.Service;
import com.robot.mvc.dispatch.ServiceBean;
import com.robot.mvc.utils.ToolsKit;

import java.lang.reflect.Method;
import java.util.*;

public class BeanHelper {

    private static BeanHelper beanHelper = new BeanHelper();
    private static Set<String> excludedMethodName = null;
    /**车辆ServiceBean映射关系集合，key为车辆或设备的标识符*/
    private static final Map<String, ServiceBean> SERVICE_BEAN_MAP = new HashMap<>();

    public static BeanHelper duang() {
        if (null == excludedMethodName) {
            excludedMethodName = ToolsKit.buildExcludedMethodName();
        }
        return beanHelper;
    }

    public static Map<String, ServiceBean> getServiceBeanMap() {
        return SERVICE_BEAN_MAP;
    }

    public Map<String, ServiceBean> toServiceBean() {
        if (SERVICE_BEAN_MAP.isEmpty()) {
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
                    // 如果没有指定，则以Service命令作为标识符
                    if (ToolsKit.isEmpty(key)) {
                        int endIndex = serviceClass.getSimpleName().toLowerCase().indexOf("service");
                        if(endIndex > -1) {
                            key = serviceClass.getSimpleName().substring(0, endIndex);
                        }
                    }
                    Map<String, Method> methodMap = new HashMap<>();
                    for (Method method : methodList) {
                        methodMap.put(method.getName().toLowerCase(), method);
                    }
                    SERVICE_BEAN_MAP.put(key, new ServiceBean(serviceClass, methodMap));
                }
            }
        }
        return SERVICE_BEAN_MAP;
    }


}
