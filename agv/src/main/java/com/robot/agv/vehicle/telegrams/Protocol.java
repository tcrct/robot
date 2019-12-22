package com.robot.agv.vehicle.telegrams;

/**
 * 协议对象
 *
 * @author laotang
 */
public class Protocol implements java.io.Serializable {

    /**设备/车辆 ID*/
    private String deviceId;
    /**功能指令*/
    private String commandKey;
    /**参数*/
    private String params;
    /**方向,上下行*/
    private String direction;
    /**CRC验证码*/
    private String crc ;

    private Protocol(String deviceId, String direction, String commandKey, String params, String crc) {
        this.deviceId = deviceId;
        this.direction = direction;
        this.commandKey = commandKey;
        this.params = params;
        this.crc = crc;
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

    public String getCrc() {
        return crc;
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
