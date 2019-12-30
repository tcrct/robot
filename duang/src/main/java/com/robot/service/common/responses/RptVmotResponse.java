package com.robot.service.common.responses;

import com.robot.service.common.BaseResponse;

/**
 *上报动作到位信息
 *
 * Created by laotang on 2019/10/15.
 */
public class RptVmotResponse extends BaseResponse {

    public RptVmotResponse(String deviceId, String params) {
        super(deviceId, params);
    }

    @Override
    public String cmd() {
        return "rptvmot";
    }


}
