package com.robot.service.common.responses;

import com.robot.core.Sensor;
import com.robot.service.common.ActionResponse;

/**
 *
 * Created by laotang on 2019/10/15.
 */
public class RptMtResponse extends ActionResponse {

    public RptMtResponse(String deviceId, String params) {
        super(deviceId, params);
    }

    // 传感器对象作参数时
    public RptMtResponse(String deviceId, Sensor sensor) {
        super(deviceId, sensor.toString());
        // 加入到缓存
        Sensor.getSensorMap().put(deviceId, sensor);
    }

    @Override
    public String cmd() {
        return "rptmt";
    }
}
