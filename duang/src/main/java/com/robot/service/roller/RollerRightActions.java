package com.robot.service.roller;

import com.robot.mvc.annotations.Action;
import com.robot.mvc.interfaces.ICommand;
import com.robot.service.common.BaseActions;
import com.robot.service.common.requests.VehicleMoveRequest;
import com.robot.service.common.requests.set.SetOutRequest;
import com.robot.service.common.requests.set.SetVmotRequest;
import com.robot.service.common.responses.RptMtResponse;

import java.util.Arrays;
import java.util.List;

/**
 * 滚筒输送线指令集(右边工作台，RFID卡号为1)
 * 只需实现请求队列编排
 *
 * @author Laotang
 */
@Action
public class RollerRightActions extends BaseActions {

    /**动作组合名称，要与工厂概述中的动作名称一样*/
    private static final String ACTION_KEY = "RollerRight";
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
    @Override
    public String vehicleId() {
        return VEHICLE_ID;
    }

    @Override
    public String deviceId() {
        return DEVICE_ID;
    }

    /**
     * 车辆或串口模块地址的名称，确定该指令集仅制于该车辆或串口
     * 如果是TCP/UDP方式，则以 IP:PORT 方式作标记，须与OpenTCS里的车辆host,port一致
     * 一般指车辆
     *
     * @return 车辆/串口模块名称
     */
//    @Override
//    public String deviceId() {
//        return DEVICE_ID;
//    }


    /***
     * 添加该指令集的请求动作，动作顺序以存放顺序一致，即在位置靠前的先执行。
     *
     * 工作顺序：
     * 1，设备滚筒从左到右转(背向设备)
     * 2，挡片上升[可选，为保证挡片处在上升完成状态]
     * 3，车辆传送带从左到右转(背向设备)
     * 4，等待经过第一个传感器后的报文
     * 5，车辆移动到另一个点(提前离开，提高效率)
     * 6，接收到传送设备上报的传送到位请求
     * 7，车辆传送带停止转动
     *
     * @param requestList   要执行的请求指令的有序数组
     */
    @Override
    public void add(List<ICommand> requestList) {
        requestList.addAll(Arrays.asList(
                new SetOutRequest(DEVICE_ID, "1::1"),
                new SetOutRequest(DEVICE_ID, "3::0"),
                new SetVmotRequest(VEHICLE_ID, "1::1"),
                //第一个点已经通过
                new RptMtResponse(DEVICE_ID, "1::0::0::0::0::0"),
//                new RptMtResponse(DEVICE_ID, new Sensor.Builder().element(0,"1").build()),
                //将车辆移走
//                new VehicleMoveRequest(VEHICLE_ID),
                // 继续执行以下指令
                new RptMtResponse(DEVICE_ID, "1::0::1::1::0::0"),
//                new RptMtResponse(DEVICE_ID, new Sensor.Builder().element(0,"1").element(2,"1").element(3,"1").build()),
                new SetOutRequest(DEVICE_ID, "1::0")
                // 等待车辆上报指令报文，配对不成功或没收到报文会一直阻塞队列，直至配对成功，配对成功后则进入一下个请求
//                new RptMtResponse(VEHICLE_ID, RptMtEnum.SUCCESS),
                // 另一种写法
//                new SetVmotRequest(new Protocol.Builder().deviceId(VEHICLE_ID).params(SetVmotEnum.LEFT_TO_RIGHT_STOP).build())
        ));
    }
}
