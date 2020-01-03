package com.robot.core;

import com.robot.numes.RobotEnum;
import com.robot.utils.CrcUtil;

import java.util.*;

/**
 * 代码：Sensor sensor = new Sensor.Builder().element(1,"1")
 *                                                                          .element(2,"0")  //有多个时
 *                                                                         .element(3,"1")  //有多个时
 *                                                                         .element(4,"0")  //有多个时
 *                                                                         .build();
 *
 * 将参数： 1::0::1::0  转换为  0_1|1_0|2_1|3_0
 * 第1位的值为1，第2位的值为0，第3位的值为1，如此类推
 *
 * @author laotang
 */
public class Sensor implements java.io.Serializable {

    private static final String LINK_CHARACTER = "_";
    private static final String SQLIT_CHARACTER = "|";

    //传感器集合,key为车辆/设备ID，value为传感器对象
    private static Map<String, Sensor> SENSOR_MAP = new HashMap<>();

    private Map<Integer, String> map = new TreeMap<>();

    private String code;

    private Sensor(Map<Integer, String> map) {
        this.map.putAll(map);
    }

    public Sensor(String paramStr) {
        String[] paramsArray = paramStr.split(SQLIT_CHARACTER);
        for(String paramItem : paramsArray) {
            String[] paramItemArray = paramItem.split(LINK_CHARACTER);
            map.put(Integer.parseInt(paramItemArray[0]), paramItemArray[1]);
        }
    }

    public boolean isWith(String params) {
        return isWith(params.split(RobotEnum.PARAMLINK.getValue()));
    }

    public boolean isWith(String[] params) {
        List<Boolean> booleanList = new ArrayList<>(map.size());
        for(Iterator<Integer> iterator = map.keySet().iterator(); iterator.hasNext();) {
            Integer index = iterator.next();
            if(params[index].equals(map.get(index))) {
                booleanList.add(true);
            }
        }
        // 个数不等
        if(booleanList.size() != map.size()) {
            return false;
        }
        for(Boolean b : booleanList) {
            if(!b) {
                return false;
            }
        }

        return true;
    }

    public static class Builder {
        private Map<Integer, String> map = new TreeMap<>();

        /**
         * 传感器参数
         * @param index 索引位置，对于程序来讲0是第1位，对应参数的第1位
         * @param value 值
         * @return
         */
        public Builder element(Integer index, String value) {
            this.map.put(index, value);
            return this;
        }
        public Sensor build() {
            return new Sensor(map);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Iterator<Map.Entry<Integer, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<Integer, String> entry = iterator.next();
            sb.append(entry.getKey()).append(LINK_CHARACTER).append(entry.getValue()).append(SQLIT_CHARACTER);
        }
        String resultString = "";
        if(sb.length() > 1) {
            resultString = sb.substring(0, sb.length()-1);
        }
        // 计算验证码
        setCode(CrcUtil.CrcVerify_Str(resultString));
        return resultString;
    }

    public static Map<String, Sensor> getSensorMap() {
        return SENSOR_MAP;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
