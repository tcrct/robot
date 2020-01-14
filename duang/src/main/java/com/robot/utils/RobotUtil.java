package com.robot.utils;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.agv.vehicle.telegrams.ProtocolParam;
import com.robot.core.AppContext;
import com.robot.core.Sensor;
import com.robot.core.handshake.HandshakeTelegram;
import com.robot.mvc.exceptions.RobotException;
import com.robot.mvc.helper.ActionHelper;
import com.robot.mvc.interfaces.IAction;
import com.robot.numes.RobotEnum;
import com.robot.service.common.ActionResponse;
import com.robot.service.common.requests.get.GetAcRequest;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RobotUtil {

    private static final Logger LOG = LoggerFactory.getLogger(RobotUtil.class);


    /***
     * 根据线名称取openTCS线路图上的车辆
     */
    public static Vehicle getVehicle(String vehicleName){
        java.util.Objects.requireNonNull(vehicleName, "车辆名称不能为空");
        return AppContext.getOpenTcsObjectService(vehicleName).fetchObject(Vehicle.class, vehicleName);
    }

    /***
     * 根据点名称取openTCS线路图上的点
     */
    public static Point getPoint(String vehicleName,String pointName){
        java.util.Objects.requireNonNull(pointName, "点名称不能为空");
        return AppContext.getOpenTcsObjectService(vehicleName).fetchObject(Point.class, pointName);
    }

    /***
     * 根据协议对象取上报卡号或预停车协议内容的点
     */
    public static String getReportPoint(Protocol protocol){
        java.util.Objects.requireNonNull(protocol, "协议对象不能为空");
        if (ProtocolUtils.isRptacProtocol(protocol.getCommandKey()) ||
                ProtocolUtils.isRptrtpProtocol(protocol.getCommandKey())) {
            return protocol.getParams().split(RobotEnum.PARAMLINK.getValue())[0];
        } else{
            throw new RobotException("取上报卡号时，协议["+ProtocolUtils.converterString(protocol)+"]指令不符， 不是上报卡号[rptac/rptrtp]指令");
        }
    }

    /***
     * 根据线名称取openTCS线路图上的车辆
     */
    public static Location getLocation(String vehicleName, String locationName){
        java.util.Objects.requireNonNull(locationName, "位置名称不能为空");
        return AppContext.getOpenTcsObjectService(vehicleName).fetchObject(Location.class, locationName);
    }

    /**
     * 取点属性值
     * @param pointName 点名称
     * @param key 属性key
     * @param defaultValue 默认值
     * @return
     */
    public static String getPointPropertiesValue(String vehicleName, String pointName, String key, String defaultValue) {
        Point point =  getPoint(vehicleName, pointName);
        java.util.Objects.requireNonNull(point, "根据["+pointName+"]找不到对应的点对象");
        Map<String,String> pointMap = point.getProperties();

        if(null == pointMap || ToolsKit.isEmpty(key)) {
            throw new RobotException("点对象属性集合或者属性关键字不能为空");
        }
        String value = pointMap.get(key);
        if(ToolsKit.isEmpty(value)) {
            LOG.info("根据["+key+"]取点对象属性值时，值不存在，返回默认值：" + defaultValue);
            return defaultValue;
        }
        RobotEnum directionEnum = getDirectionEnum(value);
        if(ToolsKit.isEmpty(directionEnum)) {
            throw new RobotException("在DirectionEnum枚举里不存在与["+value+"]对应的枚举值，请检查");
        }
        return directionEnum.getValue();
    }

    public static RobotEnum getDirectionEnum(String directionStr) {
        for (RobotEnum directionEnum : RobotEnum.values()) {
            if (directionEnum.getValue().equals(directionStr)) {
                return directionEnum;
            }
        }
        LOG.info("取["+directionStr+"]枚举对象为空");
        return null;
    }

    public static String  buildProtocolParamString (List<ProtocolParam> protocolParamList) {
        StringBuilder paramsString = new StringBuilder();
        int length = protocolParamList.size();
        if(ToolsKit.isEmpty(protocolParamList) || length == 0) {
            throw new RobotException("协议参数集合不能为空");
        }
        ProtocolParam startProtocolParam = protocolParamList.get(0);
        paramsString.append(startProtocolParam.getBefore());
        for(int i=1; i<length; i++) {
            ProtocolParam nextProtocolParam = protocolParamList.get(i);
            if(ToolsKit.isNotEmpty(nextProtocolParam)) {
                paramsString.append(RobotEnum.PARAMLINK.getValue()).append(nextProtocolParam.getBefore());
            }
        }
        // 最后一个点的处理
        ProtocolParam endProtocolParam = protocolParamList.get(length-1);
        paramsString.append(RobotEnum.PARAMLINK.getValue()).append(endProtocolParam.getAfter());
        LOG.info("创建协议字符串：{}", paramsString);
        return paramsString.toString();
    }

    /**
     * 最后执行的动作名称是否包含自定义模板集合中
     * @param currentCmd
     * @return
     */
    public static  boolean isContainActionsKey(MovementCommand currentCmd) {
        String operation = currentCmd.getOperation();
        Map<String, IAction> actionMap = ActionHelper.duang().getCustomActionsQueue();
        IAction actionTemplate = actionMap.get(operation);
        if(ToolsKit.isEmpty(actionTemplate)) {
            actionTemplate = actionMap.get(operation.toUpperCase());
            if(ToolsKit.isEmpty(actionTemplate)) {
                actionTemplate = actionMap.get(operation.toLowerCase());
            }
        }
        if(ToolsKit.isEmpty(actionTemplate)) {
            LOG.error("请先配置需要执行的自定义指令组合，名称需要一致，不区分大小写");
            return false;
        }
        return true;
    }

    /**
     * 模拟设备返回信息与验证码
     * @param response
     * @return
     */
    public static ActionResponse simulation(Response response){
        java.util.Objects.requireNonNull(response, "response is null");
        Protocol protocol = response.getProtocol();
        if (ToolsKit.isEmpty(protocol)) {
            LOG.info("模拟设备返回信息时，响应对象里的协议对象为空，返回响应对象");
            return (ActionResponse)response;
        }
        String cmdKey = protocol.getCommandKey();
        // 如果不是rpt开头的指令，则更改方向
        if (!cmdKey.startsWith("rpt")) {
            protocol.setDirection(RobotEnum.DOWN_LINK.getValue());
            // 计算出验证码
            String code = CrcUtil.CrcVerify_Str(ProtocolUtils.builderCrcString(protocol));
            protocol.setCode(code);
        }
        return new ActionResponse(protocol) {
            @Override
            public String cmd() {
                return protocol.getCommandKey();
            }
        };
    }


    /**key为车辆或设备的标识号，value为车辆标识号，即适配器标识号*/
    private static Map<String,String> ADPATER_KEY_MAP = new HashMap<>();
    /**
     * 根据车辆/设备的ID取适配器key
     * 因为有些时候，是一车辆对应多设备。所以要确认关系。
     *
     * @param deviceId  车辆/设备的ID
     * @return
     */
    public static String getAdapterByDeviceId(String deviceId) {
        String key = ADPATER_KEY_MAP.get(deviceId);
        if (ToolsKit.isEmpty(key) && deviceId.startsWith("B")) {
            Map<String, Set<String>> actionMap = ActionHelper.duang().getVehicelDeviceMap();
            if (ToolsKit.isNotEmpty(actionMap)) {
                for (Iterator<Map.Entry<String,Set<String>>> iterator = actionMap.entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry<String,Set<String>> entry = iterator.next();
                    Set<String> values = entry.getValue();
                    key = entry.getKey();
                    if (ToolsKit.isNotEmpty(values)) {
                        for (String value : values) {
                            ADPATER_KEY_MAP.put(value, key);
                        }
                    }
                }
            }
        }
        return ADPATER_KEY_MAP.get(deviceId);
    }


    private static final Map<String, List<String>> SENSOR_STATUS_MAP = new ConcurrentHashMap();
    public static void addSensorStatus(Protocol protocol) {
        String params = protocol.getParams();
        String deviceId = protocol.getDeviceId();
        List<String> sensorList = SENSOR_STATUS_MAP.get(deviceId);
        if (ToolsKit.isEmpty(sensorList)) {
            sensorList = new ArrayList<>();
        }

        if (ToolsKit.isNotEmpty(sensorList)) {
            sensorList.add(params);
            SENSOR_STATUS_MAP.put(deviceId, sensorList);
            LOG.info("车辆/设备[{}]添加传感器状态成功:{}", deviceId, sensorList);
        }
    }

    public static boolean checkSensorStatus(Protocol protocol) {
        String deviceId = protocol.getDeviceId();
        Sensor sensor = Sensor.getSensor(deviceId);
        String code = "";
        if (ToolsKit.isNotEmpty(sensor) && sensor.isWith(protocol.getParams())) {
            // 取出传感器里的code
            code = sensor.getCode();
            LOG.info("车辆/设备[{}]传感器验证参数code为[{}]", deviceId, code);
        }
        final String removeCode= code;
        if (ToolsKit.isEmpty(removeCode)) {
            return false;
        }
        ThreadUtil.execAsync(new Runnable() {
            @Override
            public void run() {
                HandshakeTelegram.duang().remove(deviceId, removeCode);
            }
        });
        return true;
    }

    public static void initVehicleStatus(String deviceId) {
        TelegramSender sender = AppContext.getTelegramSender();
        sender.sendTelegram(new GetAcRequest(deviceId, "0"));
    }

    public static final Map<String,String> DIRECTION_MAP = new HashMap<>();
    public static void sureDirection(String deviceId, Protocol protocol) {
        String params = protocol.getParams();
        String[] paramsArray = params.split(RobotEnum.PARAMLINK.getValue());
        LOG.info("{}", paramsArray);
        String direction = RobotEnum.FORWARD.getValue();
        if ("A001".equals(deviceId)) {
            if ("218".equals(paramsArray[0])) {
                direction = paramsArray[1];
                DIRECTION_MAP.put(deviceId, direction);
                LOG.info("车辆 {}当前点为{}， 方向为{}", deviceId, paramsArray[0], direction);
            }

            if ("223".equals(paramsArray[0])) {
                if (RobotEnum.BACK.getValue().equals(paramsArray[1])) {
                    direction = RobotEnum.FORWARD.getValue();
                }
                if (RobotEnum.FORWARD.getValue().equals(paramsArray[1])) {
                    direction = RobotEnum.BACK.getValue();
                }
                DIRECTION_MAP.put(deviceId, direction);
                LOG.info("车辆 {}当前点为{}， 方向为{}", deviceId, paramsArray[0], direction);
            }
        }
        if ("A002".equals(deviceId)) {
            if ("231".equals(paramsArray[0])) {
                direction = paramsArray[1];
                DIRECTION_MAP.put(deviceId, direction);
                LOG.info("车辆 {}当前点为{}， 方向为{}", deviceId, paramsArray[0], direction);
            }

            if ("213".equals(paramsArray[0])) {
                if (RobotEnum.BACK.getValue().equals(paramsArray[1])) {
                    direction = RobotEnum.FORWARD.getValue();
                }
                if (RobotEnum.FORWARD.getValue().equals(paramsArray[1])) {
                    direction = RobotEnum.BACK.getValue();
                }
                DIRECTION_MAP.put(deviceId, direction);
                LOG.info("车辆 {}当前点为{}， 方向为{}", deviceId, paramsArray[0], direction);
            }
        }

    }
}
