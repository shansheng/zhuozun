package com.qs.game.handler;

import com.qs.game.core.IThreadService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.ChannelInputShutdownReadComplete;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 心跳操作
 */
@Slf4j
@Component
@ChannelHandler.Sharable
@Qualifier("beartbeatHandler")
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {


    @Autowired
    private IThreadService threadService;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.debug("HeartbeatHandler userEventTriggered --::{},{}", ctx, evt);
        if (evt instanceof IdleStateEvent) { //心跳事件
            /*
            String key = CacheKey.RedisPrefix.USER_HEART_BEAT.KEY;
            String longId = ctx.channel().id().asLongText();
            Long incr = redisService.incrOne(key + longId);
            if (incr > 5) { //5次心跳不响应则，剔除在线组
                String uid = ctx.channel().attr(Global.attrUid).get();
                boolean b = global.delCtxFromSessionRepo(uid, ctx.channel());
                Long del = redisService.del(key + longId);
                log.info("channel id {} is death ! uid = {} , result = {} , del = {}", longId, uid, b, del);
                return;
            }
            ctx.writeAndFlush(new TextWebSocketFrame("HB"))
                    .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            */

//            IdleState state = ((IdleStateEvent) evt).state();
//            if (state == IdleState.READER_IDLE) {
//                Channel channel = ctx.channel();
//                String longId = channel.id().asLongText();
//                String uid = channel.attr(Global.attrUid).get();
//                boolean b = global.delCtxFromSessionRepo(uid, ctx);
//                channel.close();
//                log.error("channel id {} is death ! uid = {} , result = {} ", longId, uid, b);
//            }
//            Global.getSessionRepo().forEach((key, value) -> System.out.println("key = " + key + "  --  " + value));

        } else if (evt instanceof SslHandshakeCompletionEvent) { //ssl 事件
            log.info("HeartbeatHandler userEventTriggered SslHandshakeCompletionEvent --::{}", evt);
        } else if (evt instanceof ChannelInputShutdownReadComplete) {//频道已关闭
            log.info("HeartbeatHandler userEventTriggered ChannelInputShutdownReadComplete --::{}", evt);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof TextWebSocketFrame) {
            threadService.heartbeat(ctx, msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }


}
