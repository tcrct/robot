package com.robot.service.common.responses;

import com.robot.service.common.ActionResponse;

/**
 *上报RFID卡号
 *
 * Created by laotang on 2019/10/15.
 */
public class RptAcResponse extends ActionResponse {

    public RptAcResponse(String deviceId, String paramEnum) {
        super(deviceId, paramEnum);
    }

    @Override
    public String cmd() {
        return "rptac";
    }


}
