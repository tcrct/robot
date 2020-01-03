package com.robot.service.smt2;

import com.robot.core.Sensor;
import com.robot.mvc.annotations.Action;
import com.robot.mvc.interfaces.ICommand;
import com.robot.service.common.BaseActions;
import com.robot.service.common.requests.get.GetMtRequest;
import com.robot.service.common.requests.set.SetOutRequest;
import com.robot.service.common.requests.set.SetVmotRequest;
import com.robot.service.common.responses.RptMtResponse;
import com.robot.service.common.responses.RptVmotResponse;

import java.util.Arrays;
import java.util.List;

/**
 * SMT2 右边接空框动作指令集合
 *车辆将空框交给设备
 *
 *
 * @author Laotang
 */
@Action
public class SmtRightActions extends BaseActions {
    /**动作组合名称，要与工厂概述中的动作名称一样*/
    private static final String ACTION_KEY = "Smt2Right";
    /**车辆的串口模块名称*/
    private static final String VEHICLE_ID = "A033";
    /**设备的串口模块名称*/
    private static final String DEVICE_ID = "B053";

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
     * 车辆起点执行的动作：
     *  1，车辆将升降台上升到指定位置
     *  2，等待车辆上升到位回复
     *  3，设备履带转动
     *  4，车辆履带转动     *
     *  5，判断设备下层上是否有货物，根据传感器
     *  6，发送到车辆下降到指定位置
     *  7，等待车辆下降到指定位置回复
     *  8，设备履带转到
     *  9，车辆履带转到
     *
     * @param requestList   要执行的请求指令的有序数组
     */
    @Override
    public void add(List<ICommand> requestList) {
        requestList.addAll(Arrays.asList(
                new SetVmotRequest(VEHICLE_ID, "2::730"),
                new RptVmotResponse(VEHICLE_ID, "2::730"),
                new GetMtRequest(DEVICE_ID, "0"), //查询物料状态
                new RptMtResponse(DEVICE_ID, new Sensor.Builder().element(1,"0").build()),
                new SetOutRequest(DEVICE_ID, "2::1"),
                new SetVmotRequest(VEHICLE_ID, "1::-1"),
                //确认货物到达指定位置
                new RptMtResponse(DEVICE_ID, new Sensor.Builder().element(1,"1").build()),
                new GetMtRequest(DEVICE_ID, "0"), //查询物料状态
                // 等待传感器回传参数  1::0::0::0::0::0  下层有货等待
                new RptMtResponse(DEVICE_ID, new Sensor.Builder().element(0,"1").build()), // 等待传感器返回结果，第1位的参数为0时代表没有货物
                new SetVmotRequest(VEHICLE_ID, "2::20"),
                new RptVmotResponse(VEHICLE_ID, "2::20"),
                new SetVmotRequest(VEHICLE_ID, "1::1"),
                new SetOutRequest(DEVICE_ID, "1::1"),
                new RptVmotResponse(VEHICLE_ID, "1::1")
        ));
    }
}
