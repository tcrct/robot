package com.robot.agv.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.robot.agv.vehicle.telegrams.Protocol;
import org.opentcs.data.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 协议对象工具
 * Created by laotang on 2019/12/22.
 */
public class ProtocolUtils {

    private static final Logger LOG =  LoggerFactory.getLogger(ProtocolUtils.class);

    /**协议报文前缀*/
    private static final String START_PREFIX = "##";
    /**分隔符*/
    private static final String SEPARATOR = ",,";
    /**协议报文后缀*/
    private static final String END_SUFFIX = "ZZ";
    /**协议下行方向标识符*/
    public static final String DIRECTION_RESPONSE = "r";
    /**协议上行方向标识符*/
    public static final String DIRECTION_REQUEST = "s";


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

    public static String converterString(Protocol protocol) {
        StringBuilder protocolStr = new StringBuilder();
        String rawStr = builderCrcString(protocol);
        protocolStr
                .append(builderCrcString(protocol))
                .append(buildCrc(rawStr))
                .append(END_SUFFIX);
        return protocolStr.toString();
    }

    private static String builderCrcString(Protocol protocol) {

        if (!checkProtocolValue(protocol)) {
            return "";
        }

        StringBuilder protocolString = new StringBuilder();
        protocolString.append(START_PREFIX)
                .append(SEPARATOR)
                .append(protocol.getDeviceId())
                .append(SEPARATOR)
                .append(protocol.getDeviceId())
                .append(SEPARATOR)
                .append(protocol.getCommandKey())
                .append(SEPARATOR)
                .append(protocol.getParams())
                .append(SEPARATOR);

        return protocolString.toString();
    }

    /**
     * 检查报文对象值，如果为空值的话，则返回false
     * @param protocol
     * @return
     */
    private static boolean checkProtocolValue(Protocol protocol) {
        java.util.Objects.requireNonNull(protocol, "协议对象不能为空");

        Map<String,Object> protocolMap = BeanUtil.beanToMap(protocol);
        for (Iterator<Map.Entry<String,Object>> iterator = protocolMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String,Object> entry = iterator.next();
            String value = entry.getValue().toString();
            if (StrUtil.isBlank(value) || "null".equalsIgnoreCase(value)) {
                LOG.error("构建协议对象时，报文内容主体不能为空");
                return false;
            }
        }

        return true;
    }

    private static String buildCrc(String crc) {
        if (StrUtil.isNotBlank(crc)) {
            return CrcUtil.CrcVerify_Str(crc);
        }
        return "0000";
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

    /**
     * 是否是Order协议，所有不是移动指令的协议，都是订单协议
     * @param commandKey
     * @return
     */
    public static boolean isOrderProtocol(String commandKey) {
        return !isStateProtocol(commandKey);
    }

    /**
     * 是否是State协议，即车辆移动指令协议为状态协议
     * @param commandKey 指令
     * @return 如果是返回true
     */
    public static boolean isStateProtocol(String commandKey) {
        return "setrout".equalsIgnoreCase(commandKey) ||
                        "rptac".equalsIgnoreCase(commandKey) ||
                        "rptrtp".equalsIgnoreCase(commandKey);
    }

}
