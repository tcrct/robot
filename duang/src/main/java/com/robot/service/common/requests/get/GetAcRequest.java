package com.robot.service.common.requests.get;

import com.robot.service.common.BaseRequest;

/**
 * 查询RFID卡
 *
 * @author Laotang
 */
public class GetAcRequest extends BaseRequest {

    public GetAcRequest(String deviceId, String params) {
        super(deviceId, params);
    }

    @Override
    public String cmd() {
        return "getac";
    }


}
