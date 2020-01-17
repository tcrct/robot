/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle;

import com.robot.agv.vehicle.exchange.RobotCommAdapterDescription;
import com.robot.agv.vehicle.net.NetChannelType;
import com.robot.agv.vehicle.net.netty.upd.UdpServerManager;
import com.robot.core.AppContext;
import com.robot.utils.RobotUtil;
import com.robot.utils.SettingUtils;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.opentcs.kernel.extensions.controlcenter.vehicles.VehicleEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.nio.channels.NetworkChannel;

import static com.robot.agv.common.VehicleProperties.PROPKEY_VEHICLE_HOST;
import static com.robot.agv.common.VehicleProperties.PROPKEY_VEHICLE_PORT;
import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkInRange;

public class RobotCommAdapterFactory
        implements VehicleCommAdapterFactory {

    /**
     * This class's Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RobotCommAdapterFactory.class);

    /**
     * The components factory responsible to create all components needed for an example comm adapter.
     */
    private final RobotAdapterComponentsFactory componentsFactory;
    /**
     * This component's initialized flag.
     */
    private boolean initialized;

    /**
     * 创建组件工厂
     *
     * @param componentsFactory 创建特定于通信适配器的组件工厂
     */
    @Inject
    public RobotCommAdapterFactory(RobotAdapterComponentsFactory componentsFactory) {
        this.componentsFactory = requireNonNull(componentsFactory, "组件工厂对象不能为空");
    }

    @Override
    public void initialize() {
        if (initialized) {
            LOG.info("Robot适配器工厂重复初始化");
            return;
        }
        initialized = true;
        LOG.info("Robot适配器工厂初始化完成");
//        AppContext.setCommAdapterFactory(this);
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void terminate() {
        if (!initialized) {
            LOG.warn("Robot适配器工厂没有初始化");
            return;
        }
        initialized = false;
        LOG.info("Robot适配器工厂终止");
    }

    /**
     * 通讯适配器名称
     *
     * @return
     */
    @Override
    public VehicleCommAdapterDescription getDescription() {
        return new RobotCommAdapterDescription();
    }

    @Override
    @Deprecated
    public String getAdapterDescription() {
        return getDescription().getDescription();
    }

    @Override
    public boolean providesAdapterFor(Vehicle vehicle) {
        requireNonNull(vehicle, "车辆不能为空");

        if (vehicle.getProperty(PROPKEY_VEHICLE_HOST) == null) {
            LOG.warn("车辆host没有设置");
            return false;
        }

        if (vehicle.getProperty(PROPKEY_VEHICLE_PORT) == null) {
            LOG.warn("车辆port没有设置");
            return false;
        }
        try {
            checkInRange(Integer.parseInt(vehicle.getProperty(PROPKEY_VEHICLE_PORT)),
                    1024,
                    65535);
        } catch (IllegalArgumentException exc) {
            return false;
        }

        return true;
    }

    @Override
    public VehicleCommAdapter getAdapterFor(Vehicle vehicle) {
        requireNonNull(vehicle, "车辆不能为空");
        if (!providesAdapterFor(vehicle)) {
            return null;
        }

        RobotCommAdapter adapter = componentsFactory.createRobotCommAdapter(vehicle);
        adapter.getProcessModel().setVehicleHost(vehicle.getProperty(PROPKEY_VEHICLE_HOST));
        adapter.getProcessModel().setVehiclePort(
                Integer.parseInt(vehicle.getProperty(PROPKEY_VEHICLE_PORT))
        );
        LOG.info(vehicle.getName() + "#############:  " + adapter.hashCode() + "             adapter: " + adapter.getProcessModel().getName());
        // 加入到缓存集合
        AppContext.setCommAdapter(adapter);
        return adapter;
    }
}
