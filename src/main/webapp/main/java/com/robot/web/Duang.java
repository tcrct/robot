package com.robot.web;

import com.robot.config.OpenAgvConfigure;
import org.opentcs.guing.RunPlantOverview;
import org.opentcs.kernel.RunKernel;
import org.opentcs.kernelcontrolcenter.RunKernelControlCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Duang {

    private final static Logger logger = LoggerFactory.getLogger(Duang.class);

    public static void main(String[] args) {
        Duang duang = new Duang();
        try {
            duang.startOpenTcs();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    private void startOpenTcs() throws Exception {
        // 设置OpenTCS所需要的配置文件
//        java.util.Objects.requireNonNull(, "配置文件为空，请先进行设置再启动系统！");
        new OpenAgvConfigure();
        // 启动内核
        RunKernel.main(null);
        logger.warn("启动内核完成");

        // 启动内核心控制中心
        RunKernelControlCenter.main(null);
        logger.warn("启动内核心控制中心完成");


        // 启动工厂概述控制中心
        RunPlantOverview.main(null);
        logger.warn("启动工厂概述控制中心完成");

    }

}
