package com.robot.service.common.requests.set;

import com.robot.service.common.ActionRequest;

/**
 * 停车
 *
 * @author Laotang
 */
public class SetStpRequest extends ActionRequest {

    public SetStpRequest(String deviceId, String params) {
        super(deviceId,params);
    }

    @Override
    public String cmd() {
        return "setstp";
    }

}
