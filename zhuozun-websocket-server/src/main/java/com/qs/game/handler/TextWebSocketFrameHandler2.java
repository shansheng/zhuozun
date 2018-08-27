package com.qs.game.handler;

import com.qs.game.common.Global;
import com.qs.game.utils.HandlerUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * binary handler
 *
 */
@Slf4j
public class TextWebSocketFrameHandler2 extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        log.info("TextWebSocketFrameHandler2 channelRead0 {},{}", ctx, msg);
        Channel incoming = ctx.channel();
        System.out.println("TextWebSocketFrameHandler2 ctx = [" + ctx + "], msg = [" + msg + "]");
        String incomingId = HandlerUtils.getClientShortIdByChannel(incoming);
        for (Channel channel : Global.getChannelGroup()) {
            String groupClientId = HandlerUtils.getClientShortIdByChannel(channel);
            if (StringUtils.equals(incomingId, groupClientId)) {
                channel.writeAndFlush(new TextWebSocketFrame("[服务器22端返回]：" + msg.text()));
            }else {
                //发送给指定的
                channel.writeAndFlush(new TextWebSocketFrame
                        ("[来自客户端2的消息]：" + incomingId + " : " + msg.text()));

                StringBuffer sb = new StringBuffer();
                sb.append(incoming.remoteAddress()).append("->").append(msg.text());
                log.info("channelRead02222 :: {}", sb.toString());
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("TextWebSocketFrameHandler2 ctx = [" + ctx + "]");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("TextWebSocketFrameHandler2 channelRead ctx = [" + ctx + "], msg = [" + msg + "]");
        super.channelRead(ctx, msg);
    }
}
