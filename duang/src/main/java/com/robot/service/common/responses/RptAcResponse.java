package com.robot.service.common.responses;

import com.robot.service.common.BaseResponse;

/**
 *上报RFID卡号
 *
 * Created by laotang on 2019/10/15.
 */
public class RptAcResponse extends BaseResponse {

    public RptAcResponse(String deviceId, String paramEnum) {
        super(deviceId, paramEnum);
    }

    @Override
    public String cmd() {
        return "rptac";
    }


}
