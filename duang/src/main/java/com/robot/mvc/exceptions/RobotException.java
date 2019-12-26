package com.robot.mvc.exceptions;

import cn.hutool.core.util.ObjectUtil;

/**
 * Created by laotang on 2019/9/25.
 */
public class RobotException extends AbstractDuangException{

    public RobotException() {
        super();
    }

    public RobotException(String msg) {
        super(msg);
    }

    public RobotException(int code, String msg) {
        super(code, msg);
    }

    public RobotException(String msg , Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getCode() {
        return IException.FAIL_CODE;
    }

    @Override
    public String getMessage() {
        if(ObjectUtil.isEmpty(super.getMessage())) {
            return IException.FAIL_MESSAGE;
        } else {
            return super.getMessage();
        }
    }
}
