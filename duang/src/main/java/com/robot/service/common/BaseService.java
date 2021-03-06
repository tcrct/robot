package com.robot.service.common;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.agv.vehicle.telegrams.ProtocolParam;
import com.robot.numes.RobotEnum;
import com.robot.utils.ProtocolUtils;
import com.robot.utils.RobotUtil;
import com.robot.utils.SettingUtils;
import com.robot.agv.vehicle.RobotProcessModel;
import com.robot.agv.vehicle.telegrams.StateRequest;
import com.robot.mvc.exceptions.RobotException;
import com.robot.mvc.interfaces.IService;
import com.robot.utils.ToolsKit;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.Route;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static java.util.Objects.requireNonNull;

public class BaseService implements IService {

    private static final Logger LOG = LoggerFactory.getLogger(BaseService.class);
    /***
     * 公用的路径下发处理逻辑
     * @param request
     * @param response
     * @return
     */
    public String setRout(Request request, Response response) {
        if (!(request instanceof StateRequest)) {
            throw new RobotException("该请求不是移动命令请求");
        }
        StateRequest stateRequest =(StateRequest)request;
        List<ProtocolParam> protocolParamList =  getProtocolParamList(stateRequest, response);
        if (ToolsKit.isEmpty(protocolParamList)) {
            return null;
        }
        return getProtocolString(stateRequest, protocolParamList);
    }

    /**
     * 取移动命令
     * @param stateRequest
     * @return
     */
    protected Queue<MovementCommand> getCommand(StateRequest stateRequest) {
        Queue<MovementCommand> commandQueue =  stateRequest.getCommandQueue();
        if(ToolsKit.isEmpty(commandQueue)) {
           LOG.info("移动命令队列不能为空，可能是车辆提交");
           return null;
        }
        return commandQueue;
    }

    /**
     * 取车辆模型数据
     * @param stateRequest
     * @return
     */
    protected RobotProcessModel getModel(StateRequest stateRequest) {
        RobotProcessModel processModel =  stateRequest.getModel();
        if (ToolsKit.isEmpty(processModel)) {
            throw new RobotException("车辆模型对象不能为空!");
        }
        return processModel;
    }

    protected List<ProtocolParam> getProtocolParamList(StateRequest stateRequest, Response response) {
        return getProtocolParamList(stateRequest, response, "");
    }
    protected List<ProtocolParam> getProtocolParamList(StateRequest stateRequest, Response response, String orientationValue) {
        Queue<MovementCommand>  commandQueue = getCommand(stateRequest);
        List<ProtocolParam> protocolParamList = new ArrayList<>(commandQueue.size());
        for (MovementCommand command : commandQueue) {
            if (ToolsKit.isEmpty(commandQueue)) {
                return null;
            }
            String startPointName = null;
            String endPointName = null;
            String vehicleName = stateRequest.getModel().getName();
            Route.Step step = command.getStep();

//            LOG.info("##########该移动命令是否允许在车辆[{}]执行[{}]", vehicleName, step.isExecutionAllowed());

            // 当前点
            String currentPointName = step.getSourcePoint().getName();
            // 起始点
            if (ToolsKit.isEmpty(startPointName)) {
                startPointName = currentPointName;
            }
            // 下一个点
            String nextPointName = step.getDestinationPoint().getName();
            // 最终目的点，即停车点
            endPointName = stateRequest.getDestinationId();
            ProtocolParam travelParam = buildAgvTravelParamString(vehicleName, startPointName, currentPointName, nextPointName, endPointName, stateRequest, orientationValue);
            // 如果是交通管制，则最后一个点一定是停车点
            if (stateRequest.isTraffic()) {
                String after = travelParam.getAfter();
                after = RobotEnum.STOP.getValue()+after.substring(1);
                travelParam.setAfter(after);
            }
            protocolParamList.add(travelParam);
        }
        return protocolParamList;
    }

    protected String getProtocolString(StateRequest stateRequest, List<ProtocolParam> protocolParamList) {
        // 组装成参数字符串
        // 确定车辆
        String deviceId = getModel(stateRequest).getName();
        String travelParamString = RobotUtil.buildProtocolParamString(deviceId, protocolParamList);

        // 根据规则组建下发路径指令的协议指令
        Protocol protocol = new Protocol.Builder()
                .deviceId(deviceId)
                .direction(RobotEnum.UP_LINK.getValue())
                .commandKey("setrout")
                .params(travelParamString).build();
        return ProtocolUtils.converterString(protocol);
    }


    /**
     * 构建车辆行驶参数字符串
     * @return
     */
    private ProtocolParam buildAgvTravelParamString(String vehicleName, String startPointName, String currentPointName, String nextPointName,
                                                    String endPointName, StateRequest request, String orientationValue) {

        // 在工厂概述里设置的点属性关键字，例如: {"direction":"l"}， 不填写的话就是直行,确定车辆的方向，左中右
        String direction = SettingUtils.getStringByGroup("direction", "point", "direction");
        String stopMode = SettingUtils.getStringByGroup("stop.mode", "point", "stopMode");
        String orientation = SettingUtils.getStringByGroup("orientation", "point", "orientation");

        // 当前点的方向
        String directionBefore = RobotUtil.getPointPropertiesValue(vehicleName, currentPointName, direction, RobotEnum.STAIGHT_LINE.getValue());
        // 下一个点的方向
        String directionAfter = RobotUtil.getPointPropertiesValue(vehicleName, nextPointName, direction, RobotEnum.STAIGHT_LINE.getValue());
        //如果下一个点是停车(结束)点
        if (endPointName.equals(nextPointName)) {
            directionAfter = RobotUtil.getPointPropertiesValue(vehicleName, endPointName, stopMode, RobotEnum.STOP.getValue());
        }
        // 确定车辆是前进还是后退
        String around = orientationValue;
        if (ToolsKit.isEmpty(around)) {
            around = RobotUtil.getPointPropertiesValue(vehicleName, currentPointName, orientation, RobotEnum.FORWARD.getValue());
        }
        directionBefore += around + currentPointName;
        directionAfter += around + nextPointName;
        Point.Type beforePointType = RobotUtil.getPoint(vehicleName, currentPointName).getType();
        Point.Type afterPointType = RobotUtil.getPoint(vehicleName, nextPointName).getType();
        return new ProtocolParam(directionBefore, beforePointType, directionAfter, afterPointType);
    }

}
