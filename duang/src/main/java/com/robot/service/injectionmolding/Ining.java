package com.robot.service.injectionmolding;

import com.robot.mvc.annotations.Action;
import com.robot.mvc.interfaces.ICommand;
import com.robot.service.common.BaseActions;
import com.robot.service.common.requests.VehicleMoveRequest;
import com.robot.service.common.requests.set.SetVmotRequest;
import com.robot.service.common.responses.RptVmotResponse;

import java.util.Arrays;
import java.util.List;

/**
 * 入料，即注塑机将货物推到小车
 *
 * @author Laotang
 */
@Action
public class Ining extends BaseActions {

    /**动作组合名称，要与工厂概述中的动作名称一样*/
    private static final String ACTION_KEY = "injectionmoldingIn";
    /**车辆的串口模块名称*/
    private static final String VEHICLE_ID = "A006";
    /**设备的串口模块名称*/
    private static final String DEVICE_ID = "B003";

    @Override
    public String actionKey() {
        return ACTION_KEY;
    }
    @Override
    public String vehicleId() {
        return VEHICLE_ID;
    }

    @Override
    public String deviceId() {
        return DEVICE_ID;
    }

    /**
     * 工作顺序：
     * 1，注塑机将推杆下降到指定位置(setvmot::1::1)
     * 2，收到下降到指定位置的回复（rptvmot::1::1）
     * 3，小车推杆伸出（setvmot::1::1）
     * 4，收到小车推杆伸出到位回复（rptvmot::1::1）
     * 5，注塑机推杆下降到底部（setvmot::1::2）
//     * 6，收到注塑机下降到底部回复（rptvmot::1::2）
     * 7，小车收回推杆（setvmot::1::-1）
     * 8，收到小车辆推杆到位回复（rptvmot::1::1）
     * 9，小车离开
     * 10，注塑机推杆上升到顶部（setvmot::1::3）
     * 11，注塑机入料口的推杆下降到底部  (setvmot::2::1)
     *
     * @param requestList   要执行的请求指令的有序数组
     */
    @Override
    public void add(List<ICommand> requestList) {
        requestList.addAll(Arrays.asList(
                new SetVmotRequest(DEVICE_ID, "1::1"),
                new RptVmotResponse(DEVICE_ID, "1::1"),
                new SetVmotRequest(VEHICLE_ID, "1::1"),
                new RptVmotResponse(VEHICLE_ID, "1::1"),
                new SetVmotRequest(DEVICE_ID, "1::2"),
//                new RptVmotResponse(DEVICE_ID, "1::2"),
                new SetVmotRequest(VEHICLE_ID, "1::-1"),
                new RptVmotResponse(VEHICLE_ID, "1::-1"),
                //车辆离开
                new VehicleMoveRequest(VEHICLE_ID),
                new SetVmotRequest(DEVICE_ID, "1::3"),
                //确定升降台在底部
                new SetVmotRequest(DEVICE_ID, "2::1")
        ));
    }
}
