package com.robot.service.common.responses;

import com.robot.service.common.BaseResponse;

/**
 * 小车请求继续执行当前任务
 * 按启动触发该指令
 *
 * Created by laotang on 2019/10/15.
 */
public class RptGoResponse extends BaseResponse {

    public RptGoResponse(String deviceId, String paramEnum) {
        super(deviceId, paramEnum);
    }

    @Override
    public String cmd() {
        return "rptgo";
    }


}
