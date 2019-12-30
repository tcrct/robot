package com.robot.service.common;

import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.mvc.interfaces.ICommand;
import com.robot.numes.RobotEnum;
import com.robot.utils.ToolsKit;

/**
 * 结果返回基类
 * 模拟车辆/设备的上报报文内容，占位用
 *
 * Created by laotang on 2019/10/15.
 */
public abstract class BaseResponse implements ICommand {

    private Protocol protocol;
    protected String deviceId;
    private String sensor;

    public BaseResponse(String deviceId, String params) {
        this(new Protocol.Builder().deviceId(deviceId).params(params).build());
    }

    private BaseResponse(Protocol protocol) {
        this.protocol = java.util.Objects.requireNonNull(protocol, "协议对象不能为空");
        if(ToolsKit.isNotEmpty(protocol.getDeviceId())) {
            this.deviceId = protocol.getDeviceId();
        }
    }

    public BaseRequest toRequest() throws Exception {
        String cmdKey = cmd();
        this.protocol = java.util.Objects.requireNonNull(protocol, "协议对象不能为空");
        // 设置为上行，模拟成车辆提交
        protocol.setDirection(RobotEnum.UP_LINK.getValue());
        BaseRequest baseRequest = new BaseRequest(protocol) {
            @Override
            public void updateRequestContent(Response response) {

            }

            @Override
            public String cmd() {
                return cmdKey;
            }
        };

//        if (ToolsKit.isNotEmpty(sensor)) {
//            baseRequest.getPropertiesMap().put(IRequest.SENSOR_FIELD, sensor);
//        }

        baseRequest.setRequestType(BaseResponse.class.getSimpleName().toLowerCase());
        return baseRequest;
    }

    /**
     * 命令
     * @return
     */
    public abstract String cmd();

    /**
     * 设备ID
     * @return
     */
    @Override
    public String deviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

}
