package com.mh.simplerpc.common;

import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class ObjectOutDataHandler extends ChannelOutboundHandlerAdapter {

    private static Gson json = new Gson();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//        super.write(ctx, msg, promise);
//        if (msg instanceof DefaultFullHttpResponse) {
//            super.write(ctx, msg, promise);
//            return;
//        }
//
//        if (msg instanceof DefaultFullHttpRequest) {
//            super.write(ctx, msg, promise);
//            return;
//        }

//        if (msg instanceof ByteBuf) {
//            super.write(ctx, msg, promise);
//            return;
//        }

        if (msg instanceof HttpObject) {
            super.write(ctx, msg, promise);
            return;
        }

        if (msg instanceof WebSocketFrame) {
            super.write(ctx, msg, promise);
            return;
        }



//        System.out.println("ObjectOutDataHandler");
//        System.out.println(msg.getClass().toString());
//        System.out.println(msg);


//        super.write(ctx, msg, promise);

//        super.write(ctx, new TextWebSocketFrame(json.toJson(msg)), promise);
//        ctx.fireUserEventTriggered(new TextWebSocketFrame(json.toJson(msg)));
//        WebSocketClientProtocolHandler webSocketFrameHandler = (WebSocketClientProtocolHandler) ctx.channel().pipeline().get("WebSocketClientProtocolHandler");
//        webSocketFrameHandler.channelWritabilityChanged();

        ctx.writeAndFlush(new TextWebSocketFrame(json.toJson(msg)));
//        ctx.write(new TextWebSocketFrame(json.toJson(msg)));
//        ctx.flush();
//        ctx.fireChannelRead(new TextWebSocketFrame(json.toJson(msg)));
//        ctx.fireChannelReadComplete();



    }

}
