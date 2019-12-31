package com.robot.service.common.responses;


import com.robot.service.common.ActionResponse;

/**
 *上报动作到位信息
 * 预停车指令时用
 *
 * Created by laotang on 2019/10/15.
 */
public class RptrtpResponse extends ActionResponse {

    public RptrtpResponse(String deviceId, String paramEnum) {
        super(deviceId, paramEnum);
    }

    @Override
    public String cmd() {
        return "rptrtp";
    }


}
