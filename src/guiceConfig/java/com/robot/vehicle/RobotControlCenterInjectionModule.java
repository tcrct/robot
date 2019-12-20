/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.vehicle;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.robot.vehicle.exchange.AdapterPanelComponentsFactory;
import com.robot.vehicle.exchange.RobotCommAdapterPanelFactory;
import org.opentcs.customizations.controlcenter.ControlCenterInjectionModule;

/**
 * A custom Guice module for project-specific configuration.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class RobotControlCenterInjectionModule
    extends ControlCenterInjectionModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(AdapterPanelComponentsFactory.class));

    commAdapterPanelFactoryBinder().addBinding().to(RobotCommAdapterPanelFactory.class);
  }
}
