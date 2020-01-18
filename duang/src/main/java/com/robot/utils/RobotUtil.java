package com.robot.utils;

import cn.hutool.core.thread.ThreadUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.agv.vehicle.telegrams.ProtocolParam;
import com.robot.agv.vehicle.telegrams.StateRequest;
import com.robot.core.AppContext;
import com.robot.core.Sensor;
import com.robot.core.handshake.HandshakeTelegram;
import com.robot.mvc.exceptions.RobotException;
import com.robot.mvc.helper.ActionHelper;
import com.robot.mvc.interfaces.IAction;
import com.robot.numes.RobotEnum;
import com.robot.service.common.ActionResponse;
import com.robot.service.common.requests.get.GetAcRequest;
import com.robot.service.common.requests.set.SetStpRequest;
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

    public static String  buildProtocolParamString (String deviceId, List<ProtocolParam> protocolParamList) {
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
        LOG.info("{}车辆创建协议字符串：{}", deviceId, paramsString);
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

    //交通管制的上报卡号
    public static final Map<String,String> TRAFFIC_POINT_MAP = new HashMap<>();
    public static final Map<String,String> DIRECTION_MAP = new HashMap<>();
    public static boolean INIT_START_218;
    public static void sureDirection(String deviceId, Protocol protocol) {
        String pointName = getReportPoint(protocol);
        TRAFFIC_POINT_MAP.put(deviceId, pointName);
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

    // 0是，1否
    public static Map<String, String> LockVehicleMap = new HashMap<String,String>(){{
        this.put("A001", "1");
        this.put("A002", "1");
    }};
    public static void traffic(String deviceId, Protocol protocol, boolean isInLock){
        String pointName = getReportPoint(protocol);
        String isLock = LockVehicleMap.get(deviceId);
//        boolean isInLock =
//                "225".equals(pointName) || "220".equals(pointName)
//                || "233".equals(pointName)
//                || "218".equals(pointName);

        // 如果第一个上报上卡号是218的，则退出，218一定不能是第一个上报的
        if (!INIT_START_218 && "218".equals(pointName)) {
            INIT_START_218 = true;
            LOG.info("{}上报的第一个点是218，直接退出处理: {}", deviceId, isLock);
            return;
        }

        //如果没有锁住，并进入锁区范围，则锁上
        if (("1".equals(isLock)) && isInLock) {
            INIT_START_218 = true;
            //看看另一辆车是不是在锁区里，如果是，则需要停车
            String deviceId2 = "A001".equals(deviceId) ? "A002" : "A001";
            String otherLock = LockVehicleMap.get(deviceId2);
            if (null != otherLock && "0".equals(otherLock)) {
                //当前车辆的所在点
                String currentPointName = TRAFFIC_POINT_MAP.get(deviceId);
                if (ToolsKit.isNotEmpty(currentPointName)) {
                    //如果是指定的点则立即停车，停止发送新的指令
                    if ( "225".equals(currentPointName) || "220".equals(currentPointName) || "233".equals(currentPointName)) {
                        Request request = new SetStpRequest(deviceId, "0");
                        AppContext.getTelegramSender().sendTelegram(request);
                        LOG.info("发送停车指令，{}车辆立即停车，等待放行", deviceId);
//                        AppContext.getCommAdapter(deviceId).setWaitingForAllocation(true);
                    }
                }
            }
            LockVehicleMap.put(deviceId, "0");
            LOG.info("{}进入锁定区域：{}", deviceId, LockVehicleMap);
        } // 如果是锁上的，并且在锁区范围的，则认为是驶出锁区范围，则解锁
        else  if ("0".equals(isLock) && isInLock) {
            LockVehicleMap.put(deviceId, "1");
            String deviceId2 = "A001".equals(deviceId) ? "A002" : "A001";
            String isLockOf = LockVehicleMap.get(deviceId2);
            if (null !=isLockOf && "0".equals(isLockOf )) {
                LOG.info("[{}]解除等待，继续执行移动任务, {}", deviceId2, LockVehicleMap);
                String currentPointName = TRAFFIC_POINT_MAP.get(deviceId2);
                // 重新发送路径到车辆
                Protocol moveProtocol = AppContext.getCommAdapter(deviceId2).getRequestResponseMatcher().getMoveProtocol();
                Request reReq = reSendProtocol(deviceId2, currentPointName, moveProtocol);
                AppContext.getCommAdapter(deviceId2).getSender().sendTelegram(reReq);
//                AppContext.getCommAdapter(deviceId2).setWaitingForAllocation(false);
//                AppContext.getCommAdapter(deviceId2).getRequestResponseMatcher()
//                        .checkForSendingNextRequest(deviceId2);

            }
        }
    }

    /**避让后重新发送移动请求*/
    public static Request reSendProtocol(String deviceId, String currentPoint, Protocol protocol) {
        if (!deviceId.equals(protocol.getDeviceId())) {
            throw new RobotException("提交上来的车辆：" + deviceId+"     协议中的车辆："+protocol.getDeviceId() + "，不是同一台车辆的移动协议");
        }

        String params = protocol.getParams();
        String[] paramsArray = params.split(RobotEnum.PARAMLINK.getValue());
        int index = 0;
        for (String param : paramsArray) {
            if (param.endsWith(currentPoint)) {
                break;
            }
            index++;
        }
        StringBuilder sb = new StringBuilder();
        for (int i= index; i<paramsArray.length; i++) {
            sb.append(paramsArray[i]).append(RobotEnum.PARAMLINK.getValue());
        }
        String reProtocolString = "";
        if (sb.length() >1) {
            reProtocolString = sb.substring(0, sb.length()-2);
        }
        protocol.setParams(reProtocolString);

        return new StateRequest(protocol);
    }
}
