package com.robot.service.common.responses;

import com.robot.service.common.BaseResponse;

/**
 *小车请求清除残留任务，回仓库
 *
 * Created by laotang on 2019/10/15.
 */
public class RptBackResponse extends BaseResponse {

    public RptBackResponse(String deviceId, String paramEnum) {
        super(deviceId, paramEnum);
    }

    @Override
    public String cmd() {
        return "rptback";
    }


}
