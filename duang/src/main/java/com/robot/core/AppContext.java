package com.robot.core;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.vehicle.RobotCommAdapter;
import com.robot.agv.vehicle.net.NetChannelType;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.utils.SettingUtils;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.util.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AppContext {

    private static final Logger LOG = LoggerFactory.getLogger(AppContext.class);

    /**
     * 通讯适配器
     */
    private static Map<String, RobotCommAdapter> COMM_ADAPTER = new ConcurrentHashMap<>();
    public static void setCommAdapter(RobotCommAdapter commAdapter) {
        COMM_ADAPTER.put(commAdapter.getName(), commAdapter);
    }
    public static RobotCommAdapter getCommAdapter(String name) {
        return COMM_ADAPTER.get(name);
    }

    /**
     * 大杀器----TCS的对象服务器
     */
    public static TCSObjectService getOpenTcsObjectService(String key){
        return Optional.ofNullable(getCommAdapter(key).getObjectService()).orElseThrow(NullPointerException::new);
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

    /**所有超前上报的工站/设备动作协议集合，rpt*开头的协议指令*/
    private static final Map<String ,Protocol> ADVANCE_REPORT_MAP = new HashMap<>();
    // 所有超前提交的工站/设备动作协议，key为CRC验证码
    public static Map<String, Protocol> getAdvanceReportMap() {
//        logger.info("ADVANCE_REPORT_MAP size: " + ADVANCE_REPORT_MAP.size());
        return ADVANCE_REPORT_MAP;
    }

    /**
     * 设置通讯类型
     */
    private static NetChannelType CHANNEL_TYPE = null;
    public  static void setNetChannelType(NetChannelType channelType) {
        LOG.info("Robot适配器的网络渠道类型为: {}", channelType);
        CHANNEL_TYPE = channelType;
    }
    public static NetChannelType getNetChannelType() {
        return CHANNEL_TYPE;
    }

    private static KernelServicePortal kernelServicePortal;
    public static void setKernelServicePortal(KernelServicePortal servicePortal) {
        kernelServicePortal = servicePortal;
    }
    public static KernelServicePortal getKernelServicePortal() {
        return kernelServicePortal ;
    }

    private static EventSource eventSource;
    public static void setEventSource(EventSource event) {
        eventSource = event;
    }
    public static EventSource getEventSource() {
        return eventSource;
    }
}
