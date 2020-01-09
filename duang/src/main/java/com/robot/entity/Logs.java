package com.robot.entity;

import com.duangframework.db.annotation.Entity;
import com.duangframework.db.annotation.Param;
import com.duangframework.db.entity.BaseEntity;
import com.duangframework.db.entity.IdEntity;
import com.robot.agv.vehicle.telegrams.Protocol;

import java.util.Date;

/**
 *日志记录对象
 *
 * @author Laotang
 */
@Entity(name="Sys_Log")
public class Logs extends BaseEntity {

    public static final String ENTITY_FIELD = "Sys_Log";
    private static final String OPEN_AGV_FIELD = "robot";
    private static final Date CURRENT_DATE = new Date();
    private static final String DEPARTMENT_ID = "softDev";
    private static final String PROJECT_ID = "showroom";
    private static final String COMPANY_ID = "makerwit";

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

    public Logs() {
        super(null, CURRENT_DATE, OPEN_AGV_FIELD, CURRENT_DATE, OPEN_AGV_FIELD, IdEntity.STATUS_FIELD_SUCCESS, OPEN_AGV_FIELD, DEPARTMENT_ID, PROJECT_ID, COMPANY_ID);
    }

    public Logs(Protocol protocol) {
        super(null, CURRENT_DATE, OPEN_AGV_FIELD, CURRENT_DATE, OPEN_AGV_FIELD, IdEntity.STATUS_FIELD_SUCCESS, OPEN_AGV_FIELD, DEPARTMENT_ID, PROJECT_ID, COMPANY_ID);
        this.deviceId = protocol.getDeviceId();
        this.commandKey = protocol.getCommandKey();
        this.params = protocol.getParams();
        this.direction = protocol.getDirection();
        this.code = protocol.getCode();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCommandKey() {
        return commandKey;
    }

    public void setCommandKey(String commandKey) {
        this.commandKey = commandKey;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "Logs{" +
                "deviceId='" + deviceId + '\'' +
                ", commandKey='" + commandKey + '\'' +
                ", params='" + params + '\'' +
                ", direction='" + direction + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
