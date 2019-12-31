package com.robot.service.common.requests.set;

import com.robot.service.common.ActionRequest;

/**
 * 查询车辆速度值请求
 *
 * @author Laotang
 */
public class SetSpdRequest extends ActionRequest {

    public SetSpdRequest(String deviceId, String params) {
        super(deviceId,params);
    }

    @Override
    public String cmd() {
        return "setspd";
    }

}
