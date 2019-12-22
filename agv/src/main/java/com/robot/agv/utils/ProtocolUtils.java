package com.robot.agv.utils;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.robot.agv.vehicle.telegrams.Protocol;

/**
 * 协议对象工具
 * Created by laotang on 2019/12/22.
 */
public class ProtocolUtils {

    /**协议报文前缀*/
    private static final String START_PREFIX = "##";
    /**分隔符*/
    private static final String SEPARATOR = ",,";
    /**协议报文后缀*/
    private static final String END_SUFFIX = "ZZ";


    /**
     * 根据报文内容构建协议对象
     *
     * @param telegramData 报文内容
     * @return Protocol
     */
    public static Protocol buildProtocol(String telegramData) {
        if(!checkTelegramFormat(telegramData)) {
            throw new IllegalArgumentException("报文["+telegramData+"]格式不正确");
        }

        String[] telegramArray = StrUtil.split(telegramData, SEPARATOR);
        if(ArrayUtil.isEmpty(telegramArray)) {
            throw new NullPointerException("构建协议对象时，报文内容主体不能为空");
        }

        for(String itemValue  : telegramArray){
            if(StrUtil.isEmpty(itemValue)) {
                throw new NullPointerException("协议报文的每个单元内容值不能为空");
            }
        }

        return new Protocol.Builder()
                .deviceId(telegramArray[1])
                .direction(telegramArray[2])
                .commandKey(telegramArray[3])
                .params(telegramArray[4])
                .crc(telegramArray[5])
                .build();

    }

    /**
     * 是否报文格式
     * @param telegramData
     * @return 正确返回true
     */
    public static boolean checkTelegramFormat(String telegramData) {
        return StrUtil.startWith(telegramData, START_PREFIX) &&
                        telegramData.contains(SEPARATOR) &&
                        StrUtil.endWith(telegramData, END_SUFFIX);
    }

}
