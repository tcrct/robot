package com.robot.service.roller;

import com.robot.mvc.annotations.Action;
import com.robot.mvc.interfaces.ICommand;
import com.robot.service.common.BaseActions;
import com.robot.service.common.requests.set.SetOutRequest;
import com.robot.service.common.requests.set.SetVmotRequest;
import com.robot.service.common.responses.RptVmotResponse;

import java.util.Arrays;
import java.util.List;

/**
 * 滚筒输送线指令集(左边工作台， RFID卡号为2)
 * 只需实现请求队列编排
 *
 * @author Laotang
 */
@Action(name="RollerLeft")
public class RollerLeftActions extends BaseActions {

    /**动作组合名称，要与工厂概述中的动作名称一样*/
    private static final String ACTION_KEY = "RollerLeft";
    /**车辆的串口模块名称*/
    private static final String VEHICLE_ID = "A010";
    /**设备的串口模块名称*/
    private static final String DEVICE_ID = "B030";

    /**
     * 注明该动作名称，要与openTCS里的位置类研->动作一致
     * 系统用于确定车辆到达指定位置后执行的动作指令
     * @return 动作名称
     */
    @Override
    public String actionKey() {
        return ACTION_KEY;
    }

    /**
     * 车辆或串口模块地址的名称，确定该指令集仅制于该车辆或串口
     * 如果是TCP/UDP方式，则以 IP:PORT 方式作标记，须与OpenTCS里的车辆host,port一致
     * 一般指车辆
     *
     * @return 车辆/串口模块名称
     */
    @Override
    public String vehicleId() {
        return VEHICLE_ID;
    }

    @Override
    public String deviceId() {
        return DEVICE_ID;
    }


    /**
     * 添加该指令集的请求动作，动作顺序以存放顺序一致，即在位置靠前的先执行。
     *
     * 工作顺序：
     * 3，挡片下降
     * 1，设备滚筒从左到右转动(背向设备)
     * 2，车辆传送带左到右转动(背向设备)
     * 4，接收到传送设备上报的传送到位请求
     * 5，挡片上升
     * 6，滚筒传送带停止转动
     *
     * @param requestList   要执行的请求指令的有序数组
     */
    @Override
    public void add(List<ICommand> requestList) {
        requestList.addAll(Arrays.asList(
                new SetOutRequest(DEVICE_ID, "3::1"),
                new SetOutRequest(DEVICE_ID, "1::1"),
                new SetVmotRequest(VEHICLE_ID, "1::-1"),
                new RptVmotResponse(VEHICLE_ID, "1::-1"),
                new SetOutRequest(DEVICE_ID, "3::0"),
                new SetOutRequest(DEVICE_ID, "1::0")
        ));
    }
}
