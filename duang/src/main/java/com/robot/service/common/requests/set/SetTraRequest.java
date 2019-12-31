package com.robot.service.common.requests.set;

import com.robot.service.common.ActionRequest;

/**
 * 设置循迹方式--手动测试用
 * 0表示中间循迹
 * 1表示左循迹
 * 2表示右循迹
 * 3.左转90度（动力单元固定）
 * 4.右转90度（动力单元固定）
 * 5.180度掉头（动力单元固定）
 * 6.掉头行驶（双向）
 *
 * @author Laotang
 */
public class SetTraRequest extends ActionRequest {

    public SetTraRequest(String deviceId, String param) {
        super(deviceId, param);
    }

    @Override
    public String cmd() {
        return "settra";
    }
}
