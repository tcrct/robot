package com.robot.service.common.requests.set;

import com.robot.service.common.ActionRequest;

/**
 * 设置音乐
 * 1-6为音量
 * 0关闭音乐
 *
 * @author Laotang
 */
public class SetMusicRequest extends ActionRequest {

    public SetMusicRequest(String deviceId, String param) {
        super(deviceId, param);
    }

    @Override
    public String cmd() {
        return "setmusic";
    }
}
