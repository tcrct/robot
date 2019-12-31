package com.robot.service.common.responses;

import com.robot.service.common.ActionResponse;

/**
 * 上报小车手动/自动模式
 * 1为自动，0为手动
 *
 * Created by laotang on 2019/10/15.
 */
public class RptModeResponse extends ActionResponse {

    public RptModeResponse(String deviceId, String paramEnum) {
        super(deviceId, paramEnum);
    }

    @Override
    public String cmd() {
        return "rptmode";
    }


}
