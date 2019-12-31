package com.robot.service.common.requests.set;

import com.robot.service.common.ActionRequest;

/**
 *
 * @author Laotang
 */
public class SetAvoidRequest extends ActionRequest {

    public SetAvoidRequest(String deviceId, String param) {
        super(deviceId, param);
    }

    @Override
    public String cmd() {
        return "setavoid";
    }
}
