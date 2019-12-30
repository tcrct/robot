package com.robot.service.common.requests.get;

import com.robot.service.common.BaseRequest;

/**
 * 查下磁导传感器导通状态
 *
 * @author Laotang
 */
public class GetMagRequest extends BaseRequest {

    public GetMagRequest(String deviceId, String params) {
        super(deviceId, params);
    }

    @Override
    public String cmd() {
        return "getmag";
    }
}
