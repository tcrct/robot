//package com.robot.service.console;
//
//import cn.hutool.core.util.ReflectUtil;
//import com.duangframework.db.mongodb.MongoDao;
//import com.makerwit.dto.VehicleEntryDetailedDto;
//import com.makerwit.dto.VehicleEntryListDto;
//import com.makerwit.dto.dhtmlx.DhtmlxFactory;
//import com.makerwit.entity.Logs;
//import com.openagv.core.annotations.Service;
//import com.openagv.exceptions.AgvException;
//import com.openagv.opentcs.commands.*;
//import com.openagv.opentcs.model.VehicleModelTO;
//import com.openagv.route.Route;
//import com.openagv.route.RouteHelper;
//import com.openagv.tools.ToolsKit;
//import com.robot.mvc.annotations.Service;
//import com.robot.mvc.exceptions.RobotException;
//import com.robot.utils.RobotUtil;
//import com.robot.utils.ToolsKit;
//import org.opentcs.components.kernel.services.VehicleService;
//import org.opentcs.data.TCSObjectReference;
//import org.opentcs.data.model.Point;
//import org.opentcs.data.model.Vehicle;
//import org.opentcs.drivers.vehicle.AdapterCommand;
//import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
//import org.opentcs.drivers.vehicle.management.AttachmentInformation;
//import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
//import org.opentcs.kernelcontrolcenter.vehicles.LocalVehicleEntry;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.inject.Inject;
//import java.lang.reflect.Method;
//import java.util.*;
//
//
//@Service
//public class ConsoleService {
//
//    private static final Logger logger = LoggerFactory.getLogger(ConsoleService.class);
//
//    private MongoDao<Logs> logsDao;
////    private CommAdapter adapter;
////    private VehicleService vehicleService;
//
//    /**
//     * 初始化Dao对象
//     * 这个比较特殊的写法，不建议使用
//     */
//    @Inject
//    public ConsoleService() {
////        logsDao = new MongoDao<Logs>("openAGV", Logs.class);
////        adapter = AppContext.getCommAdapter();
//    }
//
//    /**
//     * 设置车辆最新位置
//     *
//     * @param vehicleName    车辆名称
//     * @param position       点位置
//     * @param vehicleService 车辆服务
//     * @return
//     */
//    public String setPosition(String vehicleName, String position, VehicleService vehicleService) {
//        try {
////            System.out.println( logsDao);
////            System.out.println(vehicleService);
////            System.out.println( "########################count1: " + logsDao.count(new Query()));
//            sendCommAdapterCommand(new SetPositionCommand(position), vehicleName, vehicleService);
////        adapter.getProcessModel().setVehiclePosition(position);
//            return ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","success");}});
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","fail");}});
//        }
//    }
//
//    /**
//     * 设置能量等级
//     *
//     * @param vehicleName    车辆名称
//     * @param level          电量等级
//     * @param vehicleService 车辆服务
//     */
//    public String setEnergyLevel(String vehicleName, int level, VehicleService vehicleService) {
//        return sendCommAdapterCommand(new SetEnergyLevelCommand(level), vehicleName, vehicleService) ?
//                ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","success");}}) :
//                ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","fail");}});
//    }
//
//    /**
//     * 车辆暂停
//     *
//     * @param vehicleName    车辆名称
//     * @param isPaused       是否暂停
//     * @param vehicleService 车辆服务
//     * @return
//     */
//    public String setVehiclePaused(String vehicleName, boolean isPaused, VehicleService vehicleService) {
//        return sendCommAdapterCommand(new SetVehiclePausedCommand(isPaused), vehicleName, vehicleService) ?
//                ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","success");}}) :
//                ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","fail");}});
//    }
//
//    /**
//     * 最大前进速度
//     *
//     * @param vehicleName
//     * @param max
//     * @param vehicleService
//     * @return
//     */
//    public String setMaxFwdVeloTxt(String vehicleName, int max, VehicleService vehicleService) {
//        return sendCommAdapterCommand(new SetMaxFwdVeloTxtCommand(max), vehicleName, vehicleService) ?
//                ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","success");}}) :
//                ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","fail");}});
//    }
//
//    /**
//     * 设置车辆方向
//     *
//     * @param vehicleName    车辆名称
//     * @param angle          方向角度数
//     * @param vehicleService 车辆服务
//     * @return
//     */
//    public String setOrientationAngle(String vehicleName, double angle, VehicleService vehicleService) {
//        return sendCommAdapterCommand(new SetOrientationAngleCommand(angle), vehicleName, vehicleService) ?
//                ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","success");}}) :
//                ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","fail");}});
//    }
//
//    /**
//     * 设置状态
//     *
//     * @param vehicleName    车辆名称
//     * @param state          车辆状态
//     * @param vehicleService 车辆服务
//     * @return
//     */
//    public String setState(String vehicleName, String state, VehicleService vehicleService) {
//        Vehicle.State stateEnum = Vehicle.State.valueOf(state.toUpperCase());
//        return sendCommAdapterCommand(new SetStateCommand(stateEnum), vehicleName, vehicleService) ?
//                    ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","success");}}) :
//                    ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","fail");}});
//    }
//
//    /**
//     * 重发车辆协议指令
//     *
//     * @param vehicleName    车辆名称
//     * @param vehicleService 车辆服务
//     * @return
//     */
//    public String continueVehicleMoveCommand(String vehicleName, VehicleService vehicleService) {
//        return sendCommAdapterCommand(new ContinueVehicleMoveCommand(), vehicleName, vehicleService) ?
//                ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","success");}}) :
//                ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","fail");}});
//    }
//
//    /**
//     * 设置手动自动模式
//     *
//     * @param vehicleName    车辆名称
//     * @param enabled        是否自动, false时为自动模式
//     * @param vehicleService 车辆服务
//     * @return
//     */
//    public String setSingleStepModeEnabled(String vehicleName, boolean enabled, VehicleService vehicleService) {
//
//        boolean isSuccess =  sendCommAdapterCommand(new SetSingleStepModeEnabledCommand(enabled), vehicleName, vehicleService);
//
//        if(isSuccess && !enabled) {
//            isSuccess = sendCommAdapterCommand(new TriggerCommand(), vehicleName, vehicleService);
//        }
//        return isSuccess ? ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","success");}}) :
//                ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","fail");}});
//    }
//
//    /***
//     * 手动模式时，下一步按钮
//     * @param vehicleName 车辆名称
//     * @param vehicleService 车辆服务
//     * @return
//     */
//    public String nextStepButton(String vehicleName, VehicleService vehicleService) {
//        return sendCommAdapterCommand(new NextStepButtonCommand(), vehicleName, vehicleService) ?
//                ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","success");}}) :
//                ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","fail");}});
//    }
//
//    /**
//     * 暂停车辆
//     * @param vehicleName 车辆名称
//     * @param vehiclePaused 是否暂停, true为暂停
//     * @param vehicleService 车辆服务
//     * @return
//     */
//    public String vehiclePaused(String vehicleName, boolean vehiclePaused, VehicleService vehicleService) {
//        return sendCommAdapterCommand(new SetVehiclePausedCommand(vehiclePaused), vehicleName, vehicleService) ?
//                ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","success");}}) :
//                ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","fail");}});
//    }
//
//
//    /***
//     * 启用适配器
//     * @param vehicleName 车辆名称
//     * @param isEnable 是否启用，true为启用
//     * @param vehicleService 车辆服务
//     * @return
//     */
//    public String enableAdapter(String vehicleName, boolean isEnable, VehicleService vehicleService) {
//        try {
//            if (isEnable) {
//                vehicleService.enableCommAdapter(getVehicleReference(vehicleService, vehicleName));
//                // 初始化车辆
//                Route route = RouteHelper.getRoutes().get(vehicleName);
//                if(ToolsKit.isNotEmpty(route)) {
//                    Method initMethod = route.getMethodMap().get("init");
//                    if(ToolsKit.isNotEmpty(initMethod)) {
//                        ReflectUtil.invoke(route.getInjectObject(), initMethod, vehicleName, vehicleService);
//                    }
//                }
//            } else {
//                vehicleService.disableCommAdapter(getVehicleReference(vehicleService, vehicleName));
//            }
//            return ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","success");}});
//        } catch (Exception ex) {
//            logger.warn("Error enabling/disabling comm adapter for {}", vehicleName, ex);
//            return ToolsKit.toJsonString(new HashMap<String, String>(){{this.put("code","fail");}});
//        }
//    }
//
//    /**
//     * 取车辆列表信息
//     * @return
//     */
//    public String getVehicleList(VehicleService vehicleService) {
//        Objects.requireNonNull(vehicleService, "vehicleService is null");
//        Set<Vehicle> vehicleSet = vehicleService.fetchObjects(Vehicle.class);
//        if (ToolsKit.isEmpty(vehicleSet)) {
//            logger.info("车辆列表数据为空");
//            return "";
//        }
//
//        List<VehicleEntryListDto> vehicleEntryDtoList = new ArrayList<>();
//       for (Vehicle vehicle : vehicleSet) {
//           LocalVehicleEntry entry = getLocalVehicleEntry(vehicleService, vehicle);
//           VehicleEntryListDto entryDto = new VehicleEntryListDto();
//           VehicleCommAdapterDescription description = entry.getAttachmentInformation().getAttachedCommAdapter();
//           VehicleProcessModelTO processModel = entry.getProcessModel();
//           entryDto.setAdapterName(description.getDescription());
//           entryDto.setEnabled(description.isSimVehicleCommAdapter());
////           entryDto.setPositionList(getVehiclePositionList(vehicleService));
//           entryDto.setState(processModel.getVehicleState().name());
//           entryDto.setVehicleName(processModel.getVehicleName());
//           vehicleEntryDtoList.add(entryDto);
//       }
//       try {
//           return DhtmlxFactory.createGridXml(vehicleEntryDtoList);
//       } catch (Exception e) {
//           return e.getMessage();
//       }
//    }
//
//    private LocalVehicleEntry getLocalVehicleEntry(VehicleService vehicleService, Vehicle vehicle) {
//        AttachmentInformation ai = vehicleService.fetchAttachmentInformation(vehicle.getReference());
//        VehicleProcessModelTO processModelTO = vehicleService.fetchProcessModel(vehicle.getReference());
//        return new LocalVehicleEntry(ai, processModelTO);
//    }
//
//    /**
//     * 根据车辆名称返回车辆信息
//     * @param vehicleName
//     * @return
//     */
//    public String getVehicle(VehicleService vehicleService, String vehicleName) {
//        Objects.requireNonNull(vehicleService, "vehicleService is null");
//        if (ToolsKit.isEmpty(vehicleName)) {
//            throw new RobotException("车辆名称不能为空");
//        }
//        Vehicle vehicle = RobotUtil.getVehicle(vehicleName);
//        LocalVehicleEntry vehicleEntry = getLocalVehicleEntry(vehicleService, vehicle);
//        VehicleCommAdapterDescription description = vehicleEntry.getAttachmentInformation().getAttachedCommAdapter();
//        VehicleModelTO processModel = (VehicleModelTO)vehicleEntry.getProcessModel();
//
//        VehicleEntryDetailedDto vehicleDetailedDto = new VehicleEntryDetailedDto();
//        vehicleDetailedDto.setAdapterEnabled(processModel.isCommAdapterEnabled());
//        vehicleDetailedDto.setPositionList(getVehiclePositionList(vehicleService));
//        vehicleDetailedDto.setStateList(getVehicleStateList());
//        vehicleDetailedDto.setOrientationAngle(String.valueOf(processModel.getOrientationAngle()));
//        vehicleDetailedDto.setEnergyLevel(processModel.getEnergyLevel());
//        vehicleDetailedDto.setMaxAcceleration(processModel.getMaxAcceleration());
//        vehicleDetailedDto.setMaxDeceleration(processModel.getMaxDeceleration());
//        vehicleDetailedDto.setMaxFwdVelocity(processModel.getMaxFwdVelocity());
//        vehicleDetailedDto.setMaxRevVelocity(processModel.getMaxRevVelocity());
//        vehicleDetailedDto.setVehiclePaused(processModel.isVehiclePaused());
//        vehicleDetailedDto.setCommandScheduling(description.isSimVehicleCommAdapter());
//
//        return ToolsKit.toJsonString(vehicleDetailedDto);
//    }
//
//
//    /***
//     * 取车辆位置集合
//     * @param vehicleService
//     * @return
//     */
//    private List<String> getVehiclePositionList(VehicleService vehicleService) {
//        Set<Point> pointSet = vehicleService.fetchObjects(Point.class);
//        List<String> pointList = new ArrayList<>(pointSet.size());
//        pointSet.stream().sorted(org.opentcs.util.Comparators.objectsByName())
//                .forEach(point -> pointList.add(point.getName()));
//        return pointList;
//    }
//
//    /**
//     * 取出车辆所有状态集合
//     * @return
//     */
//    private List<String> getVehicleStateList() {
//        Vehicle.State[] states = Vehicle.State.values();
//        List<String> stateList = new ArrayList<>(states.length);
//        Arrays.stream(states).forEach(state -> stateList.add(state.name()));
//        return stateList;
//    }
//
//    private TCSObjectReference<Vehicle> getVehicleReference(VehicleService vehicleService, String vehicleName) throws Exception {
//        return vehicleService.fetchObject(Vehicle.class, vehicleName).getReference();
//    }
//
//    private Boolean sendCommAdapterCommand(AdapterCommand command, String vehicleName, VehicleService vehicleService) {
//        try {
//            TCSObjectReference<Vehicle> vehicleRef = getVehicleReference(vehicleService, vehicleName);
//            vehicleService.sendCommAdapterCommand(vehicleRef, command);
//            return true;
//        } catch (Exception ex) {
//            logger.warn("Error sending comm adapter command '{}'", command, ex);
//            return false;
//        }
//    }
//
//}
