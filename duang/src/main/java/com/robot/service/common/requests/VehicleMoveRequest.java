package com.robot.service.common.requests;

import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.service.common.ActionRequest;

/**
 * 下发路径指令
 *
 * @author Laotang
 */
public class VehicleMoveRequest extends ActionRequest {

    public static final String CMD_FIELD = "VM_REQ";
    public static final String DEFAULT_PARAM = "duangduangduang";

    public VehicleMoveRequest(String deviceId) {
        this(deviceId, DEFAULT_PARAM);
    }
    public VehicleMoveRequest(String deviceId, String params) {
        this(new Protocol.Builder().deviceId(deviceId).params(params).build());
    }
    public VehicleMoveRequest(Protocol protocol) {
        super(protocol);
    }

    @Override
    public String cmd() {
        return CMD_FIELD;
    }
}
