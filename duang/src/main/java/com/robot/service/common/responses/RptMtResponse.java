package com.robot.service.common.responses;

import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.core.Sensor;
import com.robot.numes.RobotEnum;
import com.robot.service.common.ActionResponse;
import com.robot.utils.CrcUtil;
import com.robot.utils.ProtocolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by laotang on 2019/10/15.
 */
public class RptMtResponse extends ActionResponse {

    private static final Logger LOG = LoggerFactory.getLogger(RptMtResponse.class);

    public RptMtResponse(String deviceId, String params) {
        super(deviceId, params);
    }

    // 传感器对象作参数时
    public RptMtResponse(String deviceId, Sensor sensor) {
        super(deviceId, sensor.toString());
        // 计算验证码
        Protocol protocol = new Protocol.Builder()
                .deviceId(deviceId)
                .commandKey(cmd())
                .direction(RobotEnum.UP_LINK.getValue())
                .params(sensor.toString())
                .build();
        String crcString = ProtocolUtils.builderCrcString(protocol);
        String code = CrcUtil.CrcVerify_Str(crcString);
        protocol.setCode(code);
        sensor.setCode(code);
        LOG.info("待上传的传感器协议内容: {}, 握手验证码: {}", ProtocolUtils.converterString(protocol), code);
        // 加入到缓存
        Sensor.setSensor(deviceId, sensor);
    }

    @Override
    public String cmd() {
        return "rptmt";
    }
}
