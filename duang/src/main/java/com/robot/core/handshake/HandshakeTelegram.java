package com.robot.core.handshake;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.core.Sensor;
import com.robot.mvc.exceptions.RobotException;
import com.robot.mvc.interfaces.ICallback;
import com.robot.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/***握手电报队列，key为车辆，Value为所有需要握手的报文对象
 * 用于消息握手机制，必须进行消息握手确认处理，如握手不成功，则阻塞该车辆的消息发送，直至握手确认成功
 * 确认成功后，会移除顶部位置的报文对象
 *
 * @author Laotang
 */
public class HandshakeTelegram {

    private static final Logger LOG = LoggerFactory.getLogger(HandshakeTelegram.class);

    /**
     * 握手报文队队
     * 车辆为KEY， 队列为Value
     * ConcurrentLinkedQueue：非阻塞队列
     */
    private final static Map<String, LinkedBlockingQueue<HandshakeTelegramDto>> HANDSHAKE_TELEGRAM_QUEUE = new java.util.concurrent.ConcurrentHashMap<>();

    public static LinkedBlockingQueue<HandshakeTelegramDto> getHandshakeTelegramQueue(String deviceId) {
        if (ToolsKit.isEmpty(deviceId)) {
            return null;
        }
        return HANDSHAKE_TELEGRAM_QUEUE.get(deviceId);
    }

    private static HandshakeTelegram handshakeTelegram = new HandshakeTelegram();

    public static HandshakeTelegram duang() {
        return handshakeTelegram;
    }

    /**
     * 添加到队列最后1位
     *
     * @param telegramDto 队列对象
     */
    public void add(HandshakeTelegramDto telegramDto) {
        add(-1, telegramDto);
    }

    /**
     * 添加到队列的指定位置
     *
     * @param telegramDto 队列对象
     * @param index 指定位置
     */
    public void add(HandshakeTelegramDto telegramDto, int index) {
        add(index, telegramDto);
    }

    /**
     * 添加到队列指定位置
     * @param index
     * @param telegramDto
     */
    private void add(int index, HandshakeTelegramDto telegramDto) {
        if (ToolsKit.isEmpty(telegramDto)) {
            throw new NullPointerException("队列对象不能为空");
        }
        Response response = requireNonNull(telegramDto.getResponse(), "返回的对象不能为空");
        String deviceId = requireNonNull(response.getDeviceId(), "设备ID不能为空");
        LinkedBlockingQueue<HandshakeTelegramDto> queue = HANDSHAKE_TELEGRAM_QUEUE.get(deviceId);
        if (ToolsKit.isEmpty(queue)) {
            queue = new LinkedBlockingQueue<>();
        }
        //根据index，添加到指定位置
        if (index > -1) {
            List<HandshakeTelegramDto> dtoList = new ArrayList<>();
            queue.forEach(new Consumer<HandshakeTelegramDto>() {
                @Override
                public void accept(HandshakeTelegramDto handshakeTelegramDto) {
                    dtoList.add(handshakeTelegramDto);
                }
            });
            dtoList.add(index, telegramDto);
            queue.clear();
            queue.addAll(dtoList);
        }
        else {
            //添加到最后的位置
            queue.add(telegramDto);
        }
        HANDSHAKE_TELEGRAM_QUEUE.put(deviceId, queue);
    }

    /**
     * 移除握手队列里的报文
     *
     * @param deviceId 设备ID
     * @param code      握手code
     */
    public void remove(String deviceId, String code) {
        requireNonNull(deviceId, "设备ID不能为空");
        requireNonNull(code, "标识字段不能为空，握手消息唯一标识字段");
        LinkedBlockingQueue<HandshakeTelegramDto> queue = HANDSHAKE_TELEGRAM_QUEUE.get(deviceId);
        if (ToolsKit.isEmpty(queue)) {
            LOG .info("该车辆[{}]对应的握手队列不存在或该队列没有任何元素！", deviceId);
            return;
        }
        HandshakeTelegramDto toBeDeleteDto = requireNonNull(queue.peek(), "handshake telegram dto is null");
        Response response = requireNonNull(toBeDeleteDto.getResponse(), "response in null");
        String handshakeKey = response.getCode(); //.getHandshakeCode(); //程序计算出来的握手验证码
        if (ToolsKit.isEmpty(handshakeKey)) {
            throw new RobotException("握手key不能为空");
        }
        if (!handshakeKey.equals(code)) {
            LOG.info("系统队列中的request报文: {}", toBeDeleteDto.getRequest().getRawContent());
            LOG.info("提交上来的报文handshakeKey[" + code + "]与系统队列中handshakeKey[" + handshakeKey + "]不一致！");
            return;
        }
        //回调并移除报文
        callBackAndRemove(deviceId, queue, toBeDeleteDto);
    }


    private void callBackAndRemove(String deviceId, LinkedBlockingQueue<HandshakeTelegramDto> queue, HandshakeTelegramDto toBeDeleteDto) {
        //先复制 ??
        HandshakeTelegramDto telegramDto = new HandshakeTelegramDto(toBeDeleteDto);
        // 再移除第一位元素对象
        queue.remove();
        Request request = telegramDto.getRequest();
        LOG.info("移除车辆[" + deviceId + "]的握手报文[" +request.getRawContent() + "] 成功！");
        if( request.isRobotSend() && request.isActionResponse() &&
                "rptmt".equalsIgnoreCase(request.getProtocol().getCommandKey())) {
            Sensor.removeSensor(deviceId);
        }
         /*
        String crcCode = "";
        try {
            // 如果提前上报的缓存集合里存在对应的crcCode，则将提前上报的内容移除
            crcCode = toBeDeleteDto.getResponse().getHandshakeCode();
            if(AppContext.getAdvanceReportMap().containsKey(crcCode)) {
                AppContext.getAdvanceReportMap().remove(crcCode);
                logger.info("remove AppContext.getAdvanceReportMap[" + crcCode + "] toBeDeleteDto[" + toBeDeleteDto.toString() + "] is success!");
            }
        } catch (Exception e) {
            logger.info("remove advanceReportMap[" + crcCode + "] telegramDto[" + telegramDto.toString() + "] is fail: " + e.getMessage(), e);
        }
         */
        // 如果有回调事件，则执行回调事件，用于在工站自定义执行命令队列
        ICallback callback = telegramDto.getCallback();
        if (ToolsKit.isNotEmpty(callback)) {
            String requestId = ToolsKit.isEmpty(telegramDto.getRequest()) ? telegramDto.getResponse().getId() : telegramDto.getRequest().getId();
            String vechileName = ToolsKit.isEmpty(telegramDto.getRequest()) ? telegramDto.getResponse().getDeviceId() : "";
            if (ToolsKit.isNotEmpty(requestId)) {
                // 只有在actionKey不为空的情况下才进行回调处理
                String actionKey = telegramDto.getActionKey();
                if (ToolsKit.isNotEmpty(actionKey)) {
                    // 回调机制，告诉系统这条指令可以完结了。
                    try {
                        callback.call(actionKey, requestId, vechileName);
                    } catch (Exception e) {
                        throw new RobotException("回调移除命令时出错: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

}
