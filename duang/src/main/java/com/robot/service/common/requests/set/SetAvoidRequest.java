package com.robot.service.common.requests.set;

import com.robot.service.common.BaseRequest;

/**
 *
 * @author Laotang
 */
public class SetAvoidRequest extends BaseRequest {

    public SetAvoidRequest(String deviceId, String param) {
        super(deviceId, param);
    }

    @Override
    public String cmd() {
        return "setavoid";
    }
}
