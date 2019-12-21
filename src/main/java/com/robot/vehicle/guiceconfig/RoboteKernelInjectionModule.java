/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.vehicle.guiceconfig;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.robot.vehicle.RobotAdapterComponentsFactory;
import com.robot.vehicle.RobotCommAdapterConfiguration;
import com.robot.vehicle.RobotCommAdapterFactory;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoboteKernelInjectionModule
    extends KernelInjectionModule {
  
  private static final Logger LOG = LoggerFactory.getLogger(RoboteKernelInjectionModule.class);

  @Override
  protected void configure() {
    
    RobotCommAdapterConfiguration configuration
        = getConfigBindingProvider().get(RobotCommAdapterConfiguration.PREFIX,
            RobotCommAdapterConfiguration.class);
    
    if (!configuration.enable()) {
      LOG.info("Robot communication adapter disabled by configuration.");
      return;
    }
    
    install(new FactoryModuleBuilder().build(RobotAdapterComponentsFactory.class));
    vehicleCommAdaptersBinder().addBinding().to(RobotCommAdapterFactory.class);
  }
}
