package com.robot.agv.vehicle.telegrams;

/**
 * 协议对象
 *
 * @author laotang
 */
public class Protocol implements java.io.Serializable {

    public static final String DEVICEID_FIELD = "deviceId";
    public static final String COMMAND_KEY_FIELD = "commandKey";
    public static final String PARAMS_FIELD = "params";
    public static final String DIRECTION_FIELD = "direction";
    public static final String CRC_FIELD = "crc";

    /**设备/车辆 ID*/
    private String deviceId;
    /**功能指令*/
    private String commandKey;
    /**参数*/
    private String params;
    /**方向,上下行*/
    private String direction;
    /**CRC验证码*/
    private String code;

    private Protocol(String deviceId, String direction, String commandKey, String params, String code) {
        this.deviceId = deviceId;
        this.direction = direction;
        this.commandKey = commandKey;
        this.params = params;
        this.code = code;
    }


    public String getDeviceId() {
        return deviceId;
    }

    public String getCommandKey() {
        return commandKey;
    }

    public String getParams() {
        return params;
    }

    public String getDirection() {
        return direction;
    }

    public String getCode() {
        return code;
    }


    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setCommandKey(String commandKey) {
        this.commandKey = commandKey;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static class Builder {
        private String deviceId;
        private String direction;
        private String commandKey;
        private String params;
        private String crc;

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder direction(String direction) {
            this.direction = java.util.Objects.requireNonNull(direction, "direction is null");
            return this;
        }

        public Builder commandKey(String commandKey) {
            this.commandKey = java.util.Objects.requireNonNull(commandKey, "commandKey is null, 请确保在FunctionCommandEnum有对应的枚举值");
            return this;
        }

        public Builder params(String params) {
            this.params = params;
            return this;
        }

        public Builder crc(String crc) {
            this.crc = crc;
            return this;
        }

        public Protocol build() {
            return new Protocol(deviceId, direction, commandKey, params, crc);
        }
    }

}
