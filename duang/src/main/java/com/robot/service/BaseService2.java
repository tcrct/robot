//package com.robot.service;
//
//import cn.hutool.core.util.StrUtil;
//import com.makerwit.command.common.BaseRequest;
//import com.makerwit.command.common.Protocol;
//import com.makerwit.command.common.ProtocolParam;
//import com.makerwit.enums.DirectionEnum;
//import com.makerwit.utils.MakerwitUtil;
//import com.makerwit.utils.ProtocolFactory;
//import com.makerwit.utils.RequestCallbackFactory;
//import com.openagv.core.AppContext;
//import com.openagv.core.interfaces.IRequest;
//import com.openagv.core.interfaces.IResponse;
//import com.openagv.core.interfaces.IService;
//import com.openagv.exceptions.AgvException;
//import com.openagv.opentcs.telegrams.StateRequest;
//import com.openagv.tools.SettingUtils;
//import com.openagv.tools.ToolsKit;
//import com.robot.mvc.interfaces.IService;
//import org.opentcs.data.model.Point;
//import org.opentcs.data.order.Route;
//import org.opentcs.drivers.vehicle.MovementCommand;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.LinkedBlockingQueue;
//
///**
// * 业务处理基类
// *
// * @author Laotang
// */
//public abstract class BaseService2 implements IService {
//
//    private static final Logger logger = LoggerFactory.getLogger(BaseService2.class);
//    // 传感器状态Map，key为设备或车辆ID
//    private static final Map<String, List<String>> SENSOR_STATUS_MAP = new ConcurrentHashMap();
//
//    /**
//     * 发送报文协议
//     * @param request
//     */
//    protected void sendTelegram(BaseRequest request) {
//        RequestCallbackFactory.callback(request);
//    }
//
//    /**
//     * 用于处理没有指令对应方法时候的业务
//     * @param request
//     * @param response
//     * @return
//     */
//    public String duang(IRequest request, IResponse response){
//        System.out.println(request.getOriginalTelegram());
//        // 根据规则组建下发路径指令的协议指令
//        return new ProtocolFactory.Builder(request, response).build();
//    }
//
//    /***
//     * 公用的路径下发处理逻辑
//     * @param request
//     * @param response
//     * @return
//     */
//    public String setRout(IRequest request, IResponse response) {
//
//        StateRequest stateRequest =(StateRequest)request;
//
//        Queue<MovementCommand> commandQueue = stateRequest.getMovementCommandQueue();
//        if(ToolsKit.isEmpty(commandQueue)) {
//            throw new AgvException("移动命令队列不能为空!");
//        }
//        // 确定车辆
//        String deviceId = stateRequest.getVehicleName();
//        List<ProtocolParam> protocolParamList = new ArrayList<>();
//        List<String> nextPointNameList = new ArrayList<>();
//        Iterator<MovementCommand> iterator = commandQueue.iterator();
//        String startPointName = ""; // 起始点
//        String endPointName = ""; // 结束点
//        // 顶升AGV，单独发送一条getmag指令，确定车辆起始点的前1 后-1 左移2 右移3
//        String[] vehlieDirectionArray = null;
//        while (iterator.hasNext()) {
//            MovementCommand command = iterator.next();
//            Route.Step step= command.getStep();
//            // 当前点
//            String currentPointName = step.getSourcePoint().getName();
//            // 起始点
//            if(ToolsKit.isEmpty(startPointName)) {
//                startPointName = currentPointName;
//            }
//            // 下一个点
//            String nextPointName =   step.getDestinationPoint().getName();
//            // 最终目的点，即停车点
//            endPointName = stateRequest.getEndPointName();
//            // 车辆行驶参数对象
//            ProtocolParam travelParam = buildAgvTravelParamString(startPointName, currentPointName, nextPointName, endPointName, stateRequest);
//            protocolParamList.add(travelParam);
//            nextPointNameList.add(nextPointName);
//        }
//        //添加最后一个点
//        if(ToolsKit.isNotEmpty(endPointName)) {
//            nextPointNameList.add(endPointName);
//        }
//        //设置所有下一个点到返回结果中，让OpenTCS更新位置显示，注意：不包括起始点
//        response.setNextPointNames(nextPointNameList);
//        // 组装成参数字符串
//        String travelParamString = MakerwitUtil.buildProtocolParamString(false, protocolParamList);
//        // 根据参数字符串规则生成下发路径步骤
//        response.setPathStepList(MakerwitUtil.converterProtocolParamStringToStepList(travelParamString));
//
//        // 根据规则组建下发路径指令的协议指令
//        String protocol =  new ProtocolFactory.Builder(request, response)
//                .protocol(new Protocol.Builder()
//                        .serialPortAddress(deviceId)
//                        .deviceId(deviceId)
//                        .direction(DirectionEnum.UP_LINK)
//                        .functionCommand(MakerwitUtil.getFuncionCommand(stateRequest.getCmdKey()))
//                        .params(travelParamString).build())
//                .build();
//        commandQueue.clear();
//        return protocol;
//    }
//
//    /**
//     * 构建车辆行驶参数字符串
//     * @return
//     */
//    private ProtocolParam buildAgvTravelParamString(String startPointName, String currentPointName, String nextPointName,
//                                                    String endPointName, StateRequest request) {
//
//        // 在工厂概述里设置的点属性关键字，例如: {"direction":"l"}， 不填写的话就是直行,确定车辆的方向，左中右
//        String direction = SettingUtils.getStringByGroup("direction", "point", "direction");
//        String stopMode = SettingUtils.getStringByGroup("stop.mode", "point", "stopMode");
//        String orientation = SettingUtils.getStringByGroup("orientation", "point", "orientation");
//
//        // 当前点的方向
//        String directionBefore = MakerwitUtil.getPointPropertiesValue(currentPointName, direction, DirectionEnum.STAIGHT_LINE.getValue());
//        // 下一个点的方向
//        String directionAfter = MakerwitUtil.getPointPropertiesValue(nextPointName, direction, DirectionEnum.STAIGHT_LINE.getValue());
//        //如果下一个点是停车(结束)点
//        if (endPointName.equals(nextPointName)) {
//            directionAfter = MakerwitUtil.getPointPropertiesValue(endPointName, stopMode, DirectionEnum.STOP.getValue());
//        }
//        // 确定车辆是前进还是后退
//        String around = MakerwitUtil.getPointPropertiesValue(currentPointName, orientation, DirectionEnum.FORWARD.getValue());
//        if(ToolsKit.isTrafficControl(AppContext.getCommAdapter().getProcessModel())) {
//            double orientationAngle = AppContext.getCommAdapter().getProcessModel().getVehicleOrientationAngle();
//            if (orientationAngle == 180d) {
//                around = DirectionEnum.BACK.getValue();
//            }
//        }
////        getAround(request);
//
//        directionBefore += around + currentPointName;
//        directionAfter += around + nextPointName;
//        Point.Type beforePointType = ToolsKit.getPoint(currentPointName).getType();
//        Point.Type afterPointType = ToolsKit.getPoint(nextPointName).getType();
//        return new ProtocolParam(directionBefore, beforePointType, directionAfter, afterPointType);
//    }
//
//
//    /**上报磁导状态*/
//    protected String rptMag(IRequest request, IResponse response) {
//        setCallbackCmd(request, response);
//        // 根据规则组建下发路径指令的协议指令
//        return new ProtocolFactory.Builder(request, response).build();
//    }
//
//
//    /**上报RDIF卡号*/
//    public String rptAc(IRequest request, IResponse response) {
//        setCallbackCmd(request, response);
//        String params = MakerwitUtil.getRequestParams(request);
//        // 上报点位置的名称
//        String pointName = MakerwitUtil.getVehicleMovePoint(params);
//        response.setNextPointNames(new ArrayList<String>() {{
//            this.add(pointName);
//        }});
//
//        // 根据规则组建下发路径指令的协议指令
//        return new ProtocolFactory.Builder(request, response).build();
//    }
//
//    /**上报到达站点(预停车)*/
//    public String rptRtp(IRequest request, IResponse response) throws Exception {
//        // 根据规则组建下发路径指令的协议指令
//        String params = MakerwitUtil.getRequestParams(request);
//        // 上报点位置的名称
//        String pointName =MakerwitUtil.getVehicleMovePoint(params);
//        // 设置下一位置点
//        response.setNextPointNames(new ArrayList<String>(){{add(pointName);}});
//        return new ProtocolFactory.Builder(request, response).build();
//    }
//
//
//
//
//    /**设置速度*/
//    public String setSpd(IRequest request, IResponse response) throws Exception {
//       setCallbackCmd(request, response);
//        // 根据规则组建下发路径指令的协议指令
//        return new ProtocolFactory.Builder(request, response).build();
//    }
//
//    private void setCallbackCmd(IRequest request, IResponse response) {
//        Protocol protocol = (Protocol) request.getProtocol();
//        if (ToolsKit.isNotEmpty(protocol)) {
//            String deviceId = protocol.getDeviceId();
//            if (RequestCallbackFactory.getResponseCallbackQueueMap().containsKey(deviceId)) {
//                //将回调结果缓存到Map里对应的key中
//                LinkedBlockingQueue queue = RequestCallbackFactory.getResponseCallbackQueueMap().get(deviceId);
//                queue.add(protocol);
//                RequestCallbackFactory.getResponseCallbackQueueMap().put(deviceId, queue);
//                // 让程序跳出移动车辆显示处理逻辑部份代码
//                response.setStatus(300);
//            }
//        }
//    }
//
//    protected void updateSensorStatus(Protocol protocol, String deviceId) {
//        String params = protocol.getParams();
//        List<String> paramsList = StrUtil.splitTrim(params, DirectionEnum.PARAMLINK.getValue());
//        if (ToolsKit.isNotEmpty(paramsList)) {
//            SENSOR_STATUS_MAP.put(deviceId, paramsList);
//            logger.info("更新[ "+ deviceId +" ]传感器状态成功：" + ToolsKit.toJsonString(paramsList));
//        }
//    }
//
//}
