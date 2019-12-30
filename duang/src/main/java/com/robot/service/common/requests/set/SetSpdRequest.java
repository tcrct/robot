package com.robot.service.common.requests.set;

import com.robot.service.common.BaseRequest;

/**
 * 查询车辆速度值请求
 *
 * @author Laotang
 */
public class SetSpdRequest extends BaseRequest {

    public SetSpdRequest(String deviceId, String params) {
        super(deviceId,params);
    }

    @Override
    public String cmd() {
        return "setspd";
    }

}
