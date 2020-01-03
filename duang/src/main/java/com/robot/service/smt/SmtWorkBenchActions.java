package com.robot.service.smt;


import com.robot.core.Sensor;
import com.robot.mvc.annotations.Action;
import com.robot.mvc.interfaces.ICommand;
import com.robot.service.common.BaseActions;
import com.robot.service.common.requests.get.GetMtRequest;
import com.robot.service.common.requests.set.SetOutRequest;
import com.robot.service.common.requests.set.SetVmotRequest;
import com.robot.service.common.responses.RptMtResponse;
import com.robot.service.common.responses.RptVmotResponse;
import com.robot.service.common.responses.RptrtpResponse;

import java.util.Arrays;
import java.util.List;

@Action
public class SmtWorkBenchActions extends BaseActions {
    /**动作组合名称，要与工厂概述中的动作名称一样*/
    private static final String ACTION_KEY = "SmtWorkBench";
    /**车辆的串口模块名称*/
    private static final String VEHICLE_ID = "A009";
    /**设备的串口模块名称*/
    private static final String DEVICE_ID = "B002";

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
     *  1，等待车辆到位信息上报
     *  2，上升至指定高度(15mm)
     *  3，等待上长升至指定高度完成(15mm)
     *  4，SMT设备传送带开始从左到右转动(面向设备)，入料滚动
     *  5，车辆传送带从左到右(面向设备)转动，出料滚动
     *  6，等待SMT设备传感器上报到位信息(重点，必须确认到位才能执行下一动作)
     *  7，停止车辆传送带转动
     *  8，停止设备传送带转动
     *  9，车辆货架上升到指定高度
     *  10，SMT上是否有货
     *  11，等待SMT是否有货返回（无货则一直等待）     *
     *  12，等待货架上升高度到位
     *  13，SMT传送带从右到左转动，将货物推动车辆上（从右到左）
     *  14，SMT传送带从右到左转动，将货物推动车辆上（从右到左）
     *  15，等待车辆确认货物到位
     *  16，停止车辆入料滚动
     *  17，停止设备出料滚动
     *  18，车辆升降台下降
     *  19，等待下降到位
     *  20，车辆离开，回起点
     *
     * @param requestList   要执行的请求指令的有序数组
     */
    @Override
    public void add(List<ICommand> requestList) {
        requestList.addAll(Arrays.asList(
                new GetMtRequest(DEVICE_ID, "0"), //查询物料状态，下层入料口是否有货
                new RptMtResponse(DEVICE_ID, new Sensor.Builder().element(0,"0").build()), // 等待传感器返回结果，第1位的参数为0时代表没有货物
                new SetVmotRequest(VEHICLE_ID, "2::15"),
                new RptVmotResponse(VEHICLE_ID, "2::15"),
                new SetOutRequest(DEVICE_ID, "1::1"),
                new SetVmotRequest(VEHICLE_ID, "1::-2"),
                new RptMtResponse(DEVICE_ID, new Sensor.Builder().element(0,"1").element(5,"1").build()),//经过底部第一个与第二个传感器之间
//                new RptMtResponse(DEVICE_ID, "1::1::0::0::0::1"), //经过底部第一个与第二个传感器之间
                new SetVmotRequest(VEHICLE_ID, "1::0"),//车辆履带停
                new SetOutRequest(DEVICE_ID, "1::0"),//设备履带停
                new GetMtRequest(DEVICE_ID, "0"), //查询物料状态，上层出料口是否有货
                new RptMtResponse(DEVICE_ID, new Sensor.Builder().element(1,"1").build()),  //传感器的第2位参数为1时，代表有货
//                new RptMtResponse(DEVICE_ID, "0::1::0::0::0::0"),//转送到位
                new SetVmotRequest(VEHICLE_ID, "2::715"), //上升
                new RptVmotResponse(VEHICLE_ID, "2::715"),//上升到位
                new SetVmotRequest(VEHICLE_ID, "1::2"), //车辆履带转动，入料
                new SetOutRequest(DEVICE_ID, "2::1"),//设备履带转动,入料
                new RptVmotResponse(VEHICLE_ID, "1::2"),  // 货物到位
                new SetVmotRequest(VEHICLE_ID, "1::0"), //车辆履带停
                new SetOutRequest(DEVICE_ID, "2::0"),//设备履带停
                new SetVmotRequest(VEHICLE_ID, "2::0") //车辆升降台下降到0的位置
//                new RptVmotResponse(VEHICLE_ID, "2::715")
                //0::1::0::1::0::0
        ));
    }
}
