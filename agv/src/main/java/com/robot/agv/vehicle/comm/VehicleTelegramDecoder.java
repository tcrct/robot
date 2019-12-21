package com.robot.agv.vehicle.comm;

import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.telegrams.OrderResponse;
import com.robot.agv.vehicle.telegrams.StateResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 车辆电报解码
 *
 * @author Laotang
 */
public class VehicleTelegramDecoder extends StringDecoder {

    private final ConnectionEventListener<Response> eventListener;

    public VehicleTelegramDecoder(ConnectionEventListener<Response> eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        String telegramData = msg.toString(CharsetUtil.UTF_8);
        java.util.Objects.requireNonNull(telegramData, "telegramData");

        if (OrderResponse.isOrderResponse(telegramData)) {
            eventListener.onIncomingTelegram(new OrderResponse(telegramData));
        }
        else if (StateResponse.isStateResponse(telegramData)) {
            eventListener.onIncomingTelegram(new StateResponse(telegramData));
        }
    }
}
