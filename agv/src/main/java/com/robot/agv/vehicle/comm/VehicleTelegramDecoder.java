package com.robot.agv.vehicle.comm;

import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.telegrams.OrderRequest;
import com.robot.agv.vehicle.telegrams.OrderResponse;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.agv.vehicle.telegrams.StateResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 车辆电报(字符串)解码
 *
 * @author Laotang
 */
public class VehicleTelegramDecoder extends StringDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(VehicleTelegramDecoder.class);

    private final ConnectionEventListener<Response> eventListener;

    public VehicleTelegramDecoder(ConnectionEventListener<Response> eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        String telegramData = msg.toString(CharsetUtil.UTF_8);
        java.util.Objects.requireNonNull(telegramData, "报文协议内容不能为空");

        //将接收到的报文内容转换为Protocol对象
        Protocol protocol = new Protocol(telegramData);
        if (null == protocol) {
            LOG.warn("将报文内容转换为Protocol对象时出错, 退出该请求的处理: {}", telegramData);
            return;
        }

        // 如果是Order请求
        if (OrderRequest.isOrderRequest(protocol)) {
//            eventListener.onIncomingTelegram(new OrderRequest(telegramData));
        }
        // 如果是Order响应
        else if (OrderResponse.isOrderResponse(protocol)) {
            eventListener.onIncomingTelegram(new OrderResponse(telegramData));
        }
        // 如果是State响应
        else if (StateResponse.isStateResponse(protocol)) {
            eventListener.onIncomingTelegram(new StateResponse(telegramData));
        }
    }
}
