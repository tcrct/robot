package com.robot.service.common.responses;

import com.robot.service.common.BaseResponse;

/**
 * 上报扫码枪信息
 *
 * Created by laotang on 2019/10/15.
 */
public class RptScanResponse extends BaseResponse {

    public RptScanResponse(String deviceId, String paramEnum) {
        super(deviceId, paramEnum);
    }

    @Override
    public String cmd() {
        return "rptscan";
    }


}
