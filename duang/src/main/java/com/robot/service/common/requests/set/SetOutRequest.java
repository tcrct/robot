package com.robot.service.common.requests.set;

import com.robot.service.common.BaseRequest;

/**
 * 车载动作/工站动作请求
 *
 * @author Laotang
 */
public class SetOutRequest extends BaseRequest {

    public SetOutRequest(String deviceId, String params) {
        super(deviceId, params);
    }

    @Override
    public String cmd() {
        return "setout";
    }
}
