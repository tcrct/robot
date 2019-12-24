package com.robot.mvc.dispatch;

import cn.hutool.core.util.ReflectUtil;
import com.robot.mvc.interfaces.IService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServiceBean对象
 *
 * @author Laotang
 */
public class ServiceBean {

    private static final Logger logger = LoggerFactory.getLogger(ServiceBean.class);

    /**服务类*/
    private Class<?> serviceClass;
    /**服务类对象*/
    private IService serviceObj;
    /**对应的所有公用方法，不包括Object里的公用方法*/
    private Map<String, Method> methodMap = new ConcurrentHashMap<>();

    public ServiceBean() {

    }

    public ServiceBean(Class<?> serviceClass, Map<String, Method> methodMap) {
        this.serviceClass = serviceClass;
        setServiceObj();
        this.methodMap = methodMap;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    private void setServiceObj() {
        try {
            this.serviceObj = (IService) ReflectUtil.newInstance(serviceClass);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public IService getServiceObj() {
        return serviceObj;
    }

    public Map<String, Method> getMethodMap() {
        return methodMap;
    }

    public void setMethodMap(Map<String, Method> methodMap) {
        this.methodMap = methodMap;
    }
}
