package com.robot.agv.vehicle.net.netty.comm;

import com.robot.agv.common.telegrams.Response;
import com.robot.agv.utils.ProtocolUtils;
import com.robot.agv.vehicle.net.ChannelManagerFactory;
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
        // 接收到的协议
        ChannelManagerFactory.onIncomingTelegram(eventListener, telegramData);
    }
}
