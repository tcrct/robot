package com.robot.service.common.responses;

import com.robot.service.common.ActionResponse;

/**
 *
 * Created by laotang on 2019/10/15.
 */
public class RptVolResponse extends ActionResponse {

    public RptVolResponse(String deviceId, String paramEnum) {
        super(deviceId, paramEnum);
    }

    @Override
    public String cmd() {
        return "rptvol";
    }


}
