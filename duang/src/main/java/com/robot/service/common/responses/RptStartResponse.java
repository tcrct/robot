package com.robot.service.common.responses;

import com.robot.service.common.ActionResponse;

/**
 *
 * Created by laotang on 2019/10/15.
 */
public class RptStartResponse extends ActionResponse {

    public RptStartResponse(String deviceId, String paramEnum) {
        super(deviceId, paramEnum);
    }

    @Override
    public String cmd() {
        return "rptstart";
    }


}
