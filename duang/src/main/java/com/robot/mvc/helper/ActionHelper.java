package com.robot.mvc.helper;

import cn.hutool.core.util.ReflectUtil;
import com.robot.mvc.annotations.Action;
import com.robot.mvc.interfaces.IAction;
import com.robot.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ActionHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ActionHelper.class);

    private static ActionHelper actionHelper = new ActionHelper();

    public static ActionHelper duang() {
        return actionHelper;
    }

    /**自定义指令操作集合*/
    private final Map<String, IAction> CUSTOM_ACTION_QUEYE = new HashMap<>();

    public Map<String, List<String>> getVehicelDeviceMap() {
        return VEHICLE_DEVICE_MAP;
    }

    /**车辆与设备的映射集合，key为车辆，value为设备数组*/
    private final Map<String, List<String>> VEHICLE_DEVICE_MAP = new HashMap<>();
    /**
     * 自定义的指令队列集合
     * @return
     */
    public Map<String, IAction> getCustomActionsQueue() {
        return CUSTOM_ACTION_QUEYE;
    }

    public Map<String, IAction> getActions() {
        List<Class<?>> actionClassList = ClassHelper.duang().getActionClassList();
        if (ToolsKit.isEmpty(actionClassList)) {
            LOG.info("工作站的动作指令不存在，返回一个空的Map集合");
            return new HashMap<>();
        }
        try {
            for (Class<?> clazz : actionClassList) {
                IAction action = (IAction) ReflectUtil.newInstance(clazz);
                String key = action.actionKey();
                Action actionAonn = clazz.getAnnotation(Action.class);
                if (ToolsKit.isNotEmpty(actionAonn) && ToolsKit.isNotEmpty(actionAonn.name())) {
                    key = actionAonn.name();
                }
               List<String> deviceIdList = VEHICLE_DEVICE_MAP.get(action.vehicleId());
                if (ToolsKit.isEmpty(deviceIdList)) {
                    deviceIdList = new ArrayList<>();
                }
                deviceIdList.add(action.deviceId());
                VEHICLE_DEVICE_MAP.put(action.vehicleId(), deviceIdList);
                CUSTOM_ACTION_QUEYE.put(key, action);
            }
        } catch (Exception e) {
            LOG.error("取动作指令时发生异常:{}, {}", e.getMessage(), e);
        }
        printActionKey();
        return CUSTOM_ACTION_QUEYE;
    }

    private  void printActionKey() {
        List<String> keyList = new ArrayList<>(CUSTOM_ACTION_QUEYE.keySet());
        if(keyList.isEmpty()) {
            throw new NullPointerException("业务逻辑处理类不存在！");
        }
        Collections.sort(keyList);
        LOG.warn("**************** Action Key ****************");
        for (String key : keyList) {
            IAction action = CUSTOM_ACTION_QUEYE.get(key);
            LOG.info(String.format("action mapping: %s, action class: %s", key, action.getClass().getName()));
        }
    }
}
