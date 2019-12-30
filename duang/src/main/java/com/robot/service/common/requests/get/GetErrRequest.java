package com.robot.service.common.requests.get;

import com.robot.service.common.BaseRequest;

/**
 * 查询状态信息
 *
 * @author Laotang
 */
public class GetErrRequest extends BaseRequest {

    public GetErrRequest(String deviceId, String params) {
        super(deviceId, params);
    }

    @Override
    public String cmd() {
        return "geterr";
    }
}
