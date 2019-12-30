package com.robot.service.common.responses;

import com.robot.service.common.BaseResponse;

/**
 *
 * Created by laotang on 2019/10/15.
 */
public class RptStartResponse extends BaseResponse {

    public RptStartResponse(String deviceId, String paramEnum) {
        super(deviceId, paramEnum);
    }

    @Override
    public String cmd() {
        return "rptstart";
    }


}
