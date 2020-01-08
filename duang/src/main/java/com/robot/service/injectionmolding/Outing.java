package com.robot.service.injectionmolding;

import com.robot.mvc.annotations.Action;
import com.robot.mvc.interfaces.ICommand;
import com.robot.service.common.BaseActions;
import com.robot.service.common.requests.get.GetMtRequest;
import com.robot.service.common.requests.set.SetVmotRequest;
import com.robot.service.common.responses.RptVmotResponse;

import java.util.Arrays;
import java.util.List;

/***
 * 出料，即小车推送货物给注塑机
 *
 * @author Laotang
 */
@Action
public class Outing extends BaseActions {

    /**动作组合名称，要与工厂概述中的动作名称一样*/
    private static final String ACTION_KEY = "injectionmoldingOut";
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
     * 1，发送注塑机推杆到底部 (setvmot::2::1)
     * 2，回复推杆到底部(rptvmot::2::1)
     * 3，小车将推杆伸出(setvmot::1::1)
     * 4，收到小车推杆伸出回复（rptvmot::1::1）
     * 5，注塑机上升将物料往顶起来一点（setvmot::2::2）
     * 6，收到注塑机上升到位的回复（rptvmot::2::2）
     * 7，小车将推杆收回（setvmot::1::-1）
     * 8，收到小车推杆收回回复(rptvmot::1::-1)
     * 9，注塑机继续往上升，直到顶部（setvmot::2::3）
     *
     * @param requestList   要执行的请求指令的有序数组
     */
    @Override
    public void add(List<ICommand> requestList) {
        requestList.addAll(Arrays.asList(
                new SetVmotRequest(DEVICE_ID, "2::1"),
                new GetMtRequest(DEVICE_ID, "0"),
                new RptVmotResponse(DEVICE_ID, "2::1"),
                new SetVmotRequest(VEHICLE_ID, "1::1"),
                new RptVmotResponse(VEHICLE_ID, "1::1"),
                new SetVmotRequest(DEVICE_ID, "2::2"),
                new RptVmotResponse(DEVICE_ID, "2::2"),
                new SetVmotRequest(VEHICLE_ID, "1::-1"),
                new RptVmotResponse(VEHICLE_ID, "1::-1"),
                new SetVmotRequest(DEVICE_ID, "2::3")
        ));
    }
}
