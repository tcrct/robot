package com.robot.core;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.vehicle.RobotCommAdapter;
import com.robot.agv.vehicle.net.NetChannelType;
import com.robot.utils.SettingUtils;
import org.opentcs.components.kernel.services.TCSObjectService;

import java.util.HashMap;
import java.util.Map;

public class AppContext {

    /**
     * 通讯适配器
     */
    private static RobotCommAdapter COMM_ADAPTER;
    public static void setCommAdapter(RobotCommAdapter commAdapter) {
        COMM_ADAPTER = commAdapter;
    }
    public static RobotCommAdapter getCommAdapter() {
        return COMM_ADAPTER;
    }

    /**
     * 大杀器----TCS的对象服务器
     */
    public static TCSObjectService getOpenTcsObjectService(){
        return getCommAdapter().getObjectService() ;
    }


    private static Boolean isHandshakeListener = null;
    public static boolean isHandshakeListener() {
        if(null == isHandshakeListener) {
            isHandshakeListener = SettingUtils.getBoolean("com/robot/core/handshake", true);
        }
        return isHandshakeListener;
    }

    /**所有工站/设备动作请求*/
    private static final Map<String ,Request> ALL_ACTION_REQUEST = new HashMap<>();
    // 所有工站/设备的动作请求，CRC验证码作为key
    public static Map<String,Request> getCustomActionRequests() {
        return ALL_ACTION_REQUEST;
    }

    /**所有超前上报的工站/设备动作请求集合*/
    private static final Map<String ,Request> ADVANCE_REPORT_MAP = new HashMap<>();
    // 所有超前提交的工站/设备动作请求，key为CRC验证码
    public static Map<String, Request> getAdvanceReportMap() {
//        logger.info("ADVANCE_REPORT_MAP size: " + ADVANCE_REPORT_MAP.size());
        return ADVANCE_REPORT_MAP;
    }

    /**
     * 设置通讯类型
     */
    private static NetChannelType CHANNEL_TYPE = null;
    public  static void setNetChannelType(NetChannelType channelType) {
        CHANNEL_TYPE = channelType;
    }
    public static NetChannelType getNetChannelType() {
        return CHANNEL_TYPE;
    }
}
