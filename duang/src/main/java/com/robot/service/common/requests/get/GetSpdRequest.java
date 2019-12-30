package com.robot.service.common.requests.get;

import com.robot.service.common.BaseRequest;

/**
 * 查询车辆速度值
 * 速度值范围：0-100
 *
 * @author Laotang
 */
public class GetSpdRequest extends BaseRequest {

    public GetSpdRequest(String deviceId, String params) {
        super(deviceId, params);
    }

    @Override
    public String cmd() {
        return "getspd";
    }
}
