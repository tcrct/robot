package com.robot.agv.vehicle.telegrams;

/**
 * 协议对象
 *
 * @author laotang
 */
public class Protocol implements java.io.Serializable {

    /**类型标识*/
    private String type;
    /**串口模块地址*/
    private String serialPortAddress;
    /**设备ID*/
    private String deviceId;
    /**功能指令*/
    private String functionCommand;
    /**参数*/
    private String params;
    /**方向,上下行*/
    private String direction;
    /**验证码*/
    private String verificationCode ;

    public Protocol(String telegramData) {

    }

    private Protocol(String type, String serialPortAddress, String deviceId, String direction, String functionCommand, String params, String verificationCode) {
        this.type = type;
        this.serialPortAddress = serialPortAddress;
        this.deviceId = deviceId;
        this.direction = direction;
        this.functionCommand = functionCommand;
        this.params = params;
        this.verificationCode = verificationCode;
    }

    /*
    public static class Builder {
        private String type;
        private String serialPortAddress;
        private String deviceId;
        private DirectionEnum direction;
        private FunctionCommandEnum functionCommand;
        private String params;
        private String verificationCode;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder serialPortAddress(String serialPortAddress) {
            this.serialPortAddress = serialPortAddress;
            return this;
        }

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder direction(DirectionEnum direction) {
            this.direction = java.util.Objects.requireNonNull(direction, "direction is null");
            return this;
        }

        public Builder functionCommand(FunctionCommandEnum functionCommand) {
            this.functionCommand = java.util.Objects.requireNonNull(functionCommand, "functionCommand is null, 请确保在FunctionCommandEnum有对应的枚举值");
            return this;
        }

        public Builder params(String params) {
//            if(!params.contains(DirectionEnum.PARAMLINK.getValue())){
//                throw new IllegalArgumentException("自定义参数必须要包含["+DirectionEnum.PARAMLINK.getValue()+"]分隔符，请按规则编写！");
//            }
            this.params = params;
            return this;
        }

        public Builder params(IParamEnum paramEnum) {
            this.params = paramEnum.getValue();
            return this;
        }

        public Builder verificationCode(String verificationCode) {
            this.verificationCode = verificationCode;
            return this;
        }

        public Protocol build() {
            String directionStr = ToolsKit.isEmpty(direction) ? "" : direction.getValue();
            String functionCommandStr = ToolsKit.isEmpty(functionCommand) ? "" : functionCommand.getValue();
            return new Protocol(type, serialPortAddress, deviceId, directionStr, functionCommandStr, params, verificationCode);
        }
    }

    public String getType() {
        return type;
    }

    public String getSerialPortAddress() {
        return serialPortAddress;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(DirectionEnum direction) {
        this.direction = direction.getValue();
    }

    public void setFunctionCommand(FunctionCommandEnum functionCommand) {
        this.functionCommand = functionCommand.getValue();
    }

    public void setSerialPortAddress(String serialPortAddress) {
        this.serialPortAddress = serialPortAddress;
    }

    public String getFunctionCommand() {
        return functionCommand;
    }

    public String getParams() {
        return params;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
    */
}
