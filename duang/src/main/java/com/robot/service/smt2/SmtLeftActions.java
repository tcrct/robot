package com.robot.service.smt2;

import com.robot.mvc.interfaces.ICommand;
import com.robot.service.common.BaseActions;
import com.robot.service.common.requests.set.SetOutRequest;
import com.robot.service.common.requests.set.SetVmotRequest;
import com.robot.service.common.responses.RptMtResponse;
import com.robot.service.common.responses.RptVmotResponse;
import sun.management.Sensor;

import java.util.Arrays;
import java.util.List;

/**
 * SMT2 左边入框动作指令集合
 *车辆将货物框交给设备，车辆从另一边将货物运输指定搁置后，将货物交给设备
 *
 * @author Laotang
 */
public class SmtLeftActions extends BaseActions {
    /**动作组合名称，要与工厂概述中的动作名称一样*/
    private static final String ACTION_KEY = "Smt2Left";
    /**车辆的串口模块名称*/
    private static final String VEHICLE_ID = "A033";
    /**设备的串口模块名称*/
    private static final String DEVICE_ID = "B052";

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
     * 车辆执行的动作：
     * 1，检查入料通道是否有货物，如有则等待
     * 2，车辆升降到指定位置
     * 3，设备履带转动
     * 4，设备履带停止
     * 5，车辆履带转动
     * 6，等待设备入料底部第2个传感器回传参数
     * 7，车辆上升到指定高度
     * 8，等待上升高度到位
     * 9，车辆履带转动
     * 10，设备履带转动
     * 11，传感器上传货物是否已经离开设备
     * 12，传感器上传货物是否已经移到车辆上
     * 13，车辆下降到指定高度
     *
     * @param requestList   要执行的请求指令的有序数组
     */
    @Override
    public void add(List<ICommand> requestList) {
        requestList.addAll(Arrays.asList(
//                new ServiceRequest(DEVICE_ID, new ServiceAction.Builder().service(RptMtServiceImpl.class).method(RptMtService.CHECK_SENSOR_VALUE_METHODNAME).params(DEVICE_ID, "0", "0").build()),
                new SetVmotRequest(VEHICLE_ID, "2::10"),
                new SetOutRequest(DEVICE_ID, "1::1"),
                new SetVmotRequest(VEHICLE_ID, "1::-2"),
                new SetOutRequest(DEVICE_ID, "1::0"),
                // 等待设备入料底部第2个传感器回传参数（左->右），1代表有货物
                new RptMtResponse(DEVICE_ID, "1::0::0::0::0::0"),
                new SetVmotRequest(VEHICLE_ID, "2::715"),
                new RptVmotResponse(VEHICLE_ID, "2::715"),
                new SetVmotRequest(VEHICLE_ID, "1::2"),
//                new RptMtResponse(DEVICE_ID, new Sensor.Builder().element(1,"0").build()),
                new SetOutRequest(DEVICE_ID, "2::1"),
                // 上层第1个传感器是否没货，即货物已经移到车辆上
                new RptMtResponse(DEVICE_ID, "1::0::0::0::0::0"),
                // 车辆传感器(预停车后，必须要回复一下rptmt指令，若不然，车辆会不响应指令运作的)
                new RptMtResponse(VEHICLE_ID, "0::1::0::0::0::0"),
                new SetVmotRequest(VEHICLE_ID, "2::0")
        ));
    }
}
