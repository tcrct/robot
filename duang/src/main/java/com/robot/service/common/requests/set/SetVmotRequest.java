package com.robot.service.common.requests.set;

import com.robot.service.common.BaseRequest;

/**
 * 设置动作请求
 *
 * @author Laotang
 */
public class SetVmotRequest extends BaseRequest {

    public SetVmotRequest(String deviceId, String params) {
        super(deviceId, params);
    }

    @Override
    public String cmd() {
        return "setvmot";
    }
}
