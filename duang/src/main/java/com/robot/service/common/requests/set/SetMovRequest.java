package com.robot.service.common.requests.set;

import com.robot.service.common.BaseRequest;

/**
 * 启动--手动测试用
 * 0-缺省值(同上次)
 * 1代表前进,,-1后退
 *
 * @author Laotang
 */
public class SetMovRequest extends BaseRequest {

    public SetMovRequest(String deviceId, String param) {
        super(deviceId, param);
    }

    @Override
    public String cmd() {
        return "setmov";
    }
}
