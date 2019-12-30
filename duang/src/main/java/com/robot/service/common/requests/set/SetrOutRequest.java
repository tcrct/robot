package com.robot.service.common.requests.set;

import com.robot.service.common.BaseRequest;

/**
 * 下发路径指令
 *
 * @author Laotang
 */
public class SetrOutRequest extends BaseRequest {

    public SetrOutRequest(String deviceId, String param) {
        super(deviceId, param);
    }

    @Override
    public String cmd() {
        return "setrout";
    }
}
