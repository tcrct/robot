package com.robot.service;


import com.robot.core.AppContext;
import com.robot.service.dto.LocationOperationDto;
import com.robot.utils.RobotUtil;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.plugins.panels.loadgenerator.DriveOrderStructure;
import org.opentcs.guing.plugins.panels.loadgenerator.TransportOrderData;
import org.opentcs.guing.plugins.panels.loadgenerator.batchcreator.ExplicitOrderBatchGenerator;
import org.opentcs.guing.plugins.panels.loadgenerator.batchcreator.OrderBatchCreator;
import org.opentcs.guing.plugins.panels.loadgenerator.trigger.ThresholdOrderGenTrigger;
import org.opentcs.virtualvehicle.commands.SetPositionCommand;
import org.opentcs.virtualvehicle.commands.SetStateCommand;

import java.util.*;

/**
 * 展厅服务
 */
public class ShowRoomService {

    public static final Map<String, List<LocationOperationDto>> locationOperationMap = new HashMap<>();

    /**
     * 运行
     * @return
     */
    public boolean runAll() {
        locationOperationMap.clear();
        try {
            A006(); //注塑机
            A009(); //SMT
            A010(); // 滚筒
            A033();  // SMT2

            // 创建批量订单
            createBatchOrderRequest(locationOperationMap);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void daung(String vehicleName, String position) {
        TCSObjectReference<Vehicle> vehicleRef = RobotUtil.getVehicle(vehicleName).getReference();
        VehicleService vehicleService = AppContext.getKernelServicePortal().getVehicleService();
        // 开启适配器
        vehicleService.enableCommAdapter(vehicleRef);
        // 位置
        vehicleService.sendCommAdapterCommand(vehicleRef, new SetPositionCommand(position));
        // 设置为空闲状态
        vehicleService.sendCommAdapterCommand(vehicleRef, new SetStateCommand(Vehicle.State.IDLE));
        // 集成等级(使用该车辆进行运输订单)
        vehicleService.updateVehicleIntegrationLevel(vehicleRef, Vehicle.IntegrationLevel.TO_BE_UTILIZED);
    }

    private void A006() {
        String vehicleName = "A006";
        String position = "705";
        daung(vehicleName, position);
        //订单
        List<LocationOperationDto> locationOperationDtoList = new ArrayList<>();
        locationOperationDtoList.add(new LocationOperationDto(vehicleName, "injectionmoldingOut", "injectionmoldingOut"));
        locationOperationDtoList.add(new LocationOperationDto(vehicleName, "injectionmoldingIn", "injectionmoldingIn"));
        locationOperationDtoList.add(new LocationOperationDto(vehicleName, "Start", "NOP"));
        locationOperationMap.put(vehicleName, locationOperationDtoList);
    }

    private void A009() {
        String vehicleName = "A009";
        String position = "237";
        daung(vehicleName, position);
        List<LocationOperationDto> locationOperationDtoList = new ArrayList<>();
        locationOperationDtoList.add(new LocationOperationDto(vehicleName, "SmtWorkBench", "SmtWorkBench"));
        locationOperationDtoList.add(new LocationOperationDto(vehicleName, "StartPoint", "StartPoint"));
        locationOperationMap.put(vehicleName, locationOperationDtoList);
    }

    private void A010() {
        String vehicleName = "A010";
        String position = "1";
        daung(vehicleName, position);
        List<LocationOperationDto> locationOperationDtoList = new ArrayList<>();
        locationOperationDtoList.add(new LocationOperationDto(vehicleName, "RollerLeft", "RollerLeft"));
        locationOperationDtoList.add(new LocationOperationDto(vehicleName, "RollerRight", "RollerRight"));
        locationOperationMap.put(vehicleName, locationOperationDtoList);
    }

    private void A033() {
        String vehicleName = "A033";
        String position = "49";
        daung(vehicleName, position);
        List<LocationOperationDto> locationOperationDtoList = new ArrayList<>();
        locationOperationDtoList.add(new LocationOperationDto(vehicleName, "Smt2Left", "Smt2Left"));
        locationOperationDtoList.add(new LocationOperationDto(vehicleName, "Smt2Right", "Smt2Right"));
        locationOperationMap.put(vehicleName, locationOperationDtoList);
    }

    /**
     * 创建批量订单，持续加载
     * @param map
     */
    private void createBatchOrderRequest(Map<String, List<LocationOperationDto>> map) {
        //启动，创建订单
        KernelServicePortal kernelServicePortal =  AppContext.getKernelServicePortal();
        TransportOrderService transportOrderService = kernelServicePortal.getTransportOrderService();
        DispatcherService dispatcherService = kernelServicePortal.getDispatcherService();
        List<TransportOrderData> data = new ArrayList<>(map.size());
        for (Iterator<Map.Entry<String, List<LocationOperationDto>>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, List<LocationOperationDto>> entry = iterator.next();
            String vehicleName = entry.getKey();
            List<LocationOperationDto> locationOperationDtos = entry.getValue();
            TCSObjectReference<Vehicle> vehicleRef = RobotUtil.getVehicle(vehicleName).getReference();
            TransportOrderData transportOrderData = new TransportOrderData();
            transportOrderData.setDeadline(TransportOrderData.Deadline.PLUS_ONE_HOUR);
            transportOrderData.setIntendedVehicle(vehicleRef);
            for (LocationOperationDto dto : locationOperationDtos) {
                DriveOrderStructure driveOrderStructure = new DriveOrderStructure();
                driveOrderStructure.setDriveOrderLocation(RobotUtil.getLocation(dto.getVehicleName(), dto.getLocation()).getReference());
                driveOrderStructure.setDriveOrderVehicleOperation(dto.getOperation());
                transportOrderData.getDriveOrders().add(driveOrderStructure);
            }
            data.add(transportOrderData);
        }
        OrderBatchCreator batchGenerator = new ExplicitOrderBatchGenerator(transportOrderService, dispatcherService, data);
        batchGenerator.createOrderBatch();
        ThresholdOrderGenTrigger thresholdOrderGenTrigger = new ThresholdOrderGenTrigger(AppContext.getEventSource(),
                AppContext.getKernelServicePortal().getPlantModelService(),
                1,
                batchGenerator);
        thresholdOrderGenTrigger.setTriggeringEnabled(true);

    }


}
