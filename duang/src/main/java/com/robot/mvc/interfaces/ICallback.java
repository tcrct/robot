package com.robot.mvc.interfaces;

public interface ICallback<T> {

    void call(String deviceId, T object, String vechileName) throws Exception;

}
