package com.robot.agv.vehicle.telegrams;

import cn.hutool.core.util.StrUtil;
import com.robot.mvc.exceptions.RobotException;
import com.robot.numes.RobotEnum;
import org.opentcs.data.model.Point;

/**
 * 协议参数部份对象
 *
 * @author Laotang
 */
public class ProtocolParam implements java.io.Serializable {

    /**
     * 前半部份协议指令
     */
    private String before;
    private Point.Type beforeType;
    /**
     * 后半部份协议指令
     */
    private String after;
    private Point.Type afterType;

    public ProtocolParam() {
    }

    public ProtocolParam(String before, Point.Type beforeType, String after, Point.Type afterType) {
        this.before = before;
        this.beforeType = beforeType;
        this.after = after;
        this.afterType = afterType;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public Point.Type getBeforeType() {
        return beforeType;
    }

    public void setBeforeType(Point.Type beforeType) {
        this.beforeType = beforeType;
    }

    public Point.Type getAfterType() {
        return afterType;
    }

    public void setAfterType(Point.Type afterType) {
        this.afterType = afterType;
    }

    @Override
    public String toString() {
        if(StrUtil.isEmpty(before) || StrUtil.isEmpty(after)) {
            throw new RobotException("协议参数不能为空");
        }
        return before + RobotEnum.PARAMLINK .getValue()+ after;
    }
}
