package com.robot.service.common;


import cn.hutool.core.util.IdUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.mvc.interfaces.ICommand;
import com.robot.numes.RobotEnum;
import com.robot.utils.ProtocolUtils;
import com.robot.utils.ToolsKit;

/**
 * 请求/下达指令基类
 *
 * @author Laotang
 */
public abstract class ActionRequest extends Request implements ICommand,Comparable<ActionRequest> {

    /**请求参数*/
    private String params;
    /**接收的设备ID*/
    private String deviceId;
    /**与设备对应的车辆ID*/
    private String vehicleId;
    /**请求类型*/
    private String requestType = ActionRequest.class.getSimpleName().toLowerCase();
    /**位置索引*/
    private Double index;

    public ActionRequest(String deviceId, String param) {
        this(new Protocol.Builder().deviceId(deviceId).params(param).build());
    }

    /**
     * 构造函数
     * @param protocol 协议对象
     */
    public ActionRequest(Protocol protocol) {
        protocol.setCommandKey(cmd());
        protocol.setDirection(RobotEnum.UP_LINK.getValue());
//        setProtocol(java.util.Objects.requireNonNull(protocol, "BaseRequest: protocol对象不能为null"));
        if(ToolsKit.isNotEmpty(protocol.getDeviceId())) {
            this.deviceId = protocol.getDeviceId();
        }
//        if(MakerwitUtil.isSerialPort()) {
//            protocol.setSerialPortAddress(deviceId);
//        }
        this.params = java.util.Objects.requireNonNull(protocol.getParams(), "BaseRequest: 接收的参数不能为空");
        setProtocol(protocol);
        super.rawContent = ProtocolUtils.converterString(protocol);
        setRobotSend(true);
        super.id = IdUtil.objectId();
    }

    public Double getIndex() {
        return index;
    }

    public void setIndex(Double index) {
        this.index = index;
    }

    public String params() {
        return params;
    }

    @Override
    public String deviceId() {
        return deviceId;
    }

//    @Override
//    public String getRequestType() {
//        return requestType;
//    }

    /**
     * 排序, index数值越少的越靠前
     * @param that
     * @return
     */
    @Override
    public int compareTo(ActionRequest that) {

        if(this.index > that.index) {
            return 1;
        }
        return -1;
//        return Double.valueOf(this.index - that.index).intValue();

    }

    @Override
    public void updateRequestContent(Response response) {

    }

    /**
     * 功能函数
     * @return
     */
    public abstract String cmd();

    public void setProtocol(Protocol protocol) {
        super.protocol = protocol;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
}
