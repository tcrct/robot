package com.robot.utils;

import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.agv.vehicle.telegrams.ProtocolParam;
import com.robot.core.AppContext;
import com.robot.mvc.exceptions.RobotException;
import com.robot.numes.RobotEnum;
import org.opentcs.data.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class RobotUtil {

    private static final Logger LOG = LoggerFactory.getLogger(RobotUtil.class);

    /***
     * 根据点名称取openTCS线路图上的点
     */
    public static Point getPoint(String pointName){
        java.util.Objects.requireNonNull(pointName, "点名称不能为空");
        return AppContext.getOpenTcsObjectService().fetchObject(Point.class, pointName);
    }

    /***
     * 根据点名称取openTCS线路图上的点
     */
    public static String getPoint(Protocol protocol){
        java.util.Objects.requireNonNull(protocol, "协议对象不能为空");
        if (!ProtocolUtils.isReportStateProtocol(protocol.getCommandKey())) {
            throw new RobotException("取上报卡号时，协议["+ProtocolUtils.converterString(protocol)+"]指令不符， 不是上报卡号[rptac/rptrtp]指令");
        }
        return protocol.getParams().split(RobotEnum.PARAMLINK.getValue())[0];
    }

    /**
     * 取点属性值
     * @param pointName 点名称
     * @param key 属性key
     * @param defaultValue 默认值
     * @return
     */
    public static String getPointPropertiesValue(String pointName, String key, String defaultValue) {
        Point point =  getPoint(pointName);
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

}
