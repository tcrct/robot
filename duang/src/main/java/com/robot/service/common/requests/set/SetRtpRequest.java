package com.robot.service.common.requests.set;

import com.robot.service.common.BaseRequest;

/**
 * 预停车指令
 * 冒号分隔,,第1位表示卡号；第2位标识胶贴号为0时不起作用,,第3位表示备用标识胶贴号为0时不起作用
 *
 * @author Laotang
 */
public class SetRtpRequest extends BaseRequest {

    public SetRtpRequest(String deviceId, String param) {
        super(deviceId, param);
    }

    @Override
    public String cmd() {
        return "setrtp";
    }
}
