package com.robot.core;

import com.robot.agv.vehicle.RobotCommAdapter;
import org.opentcs.components.kernel.services.TCSObjectService;

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
}
