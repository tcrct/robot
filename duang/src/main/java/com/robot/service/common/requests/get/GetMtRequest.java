package com.robot.service.common.requests.get;

import com.robot.service.common.BaseRequest;

/**
 * 查询物料请求
 *
 * @author Laotang
 */
public class GetMtRequest extends BaseRequest {

    public GetMtRequest(String deviceId, String params) {
        super(deviceId, params);
    }

    @Override
    public String cmd() {
        return "getmt";
    }
}
