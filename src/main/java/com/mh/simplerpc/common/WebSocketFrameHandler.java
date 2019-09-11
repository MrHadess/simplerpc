package com.mh.simplerpc.common;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static Logger logger = LoggerFactory.getLogger(WebSocketFrameHandler.class);

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame) throws Exception {
        Channel channel = channelHandlerContext.channel();


        if (webSocketFrame instanceof CloseWebSocketFrame) {
//                LOGGER.info("├ 关闭与客户端[{}]链接", channel.remoteAddress());
//                socketServerHandShaker.close(channel, (CloseWebSocketFrame) webSocketFrame.retain());
//            LOGGER.info("├ 关闭与客户端[{}]链接", ((CloseWebSocketFrame)channel));

//            webSocketFrame.retain();
            channelHandlerContext.close();
            return;
        }
        // endregion
        // region 判断是否是ping消息
        if (webSocketFrame instanceof PingWebSocketFrame) {
//            LOGGER.info("├ [Ping消息]");
//            channel.write(new PongWebSocketFrame(webSocketFrame.content().retain()));
            logger.debug(String.format("Accept ping frame to channel id:%s",channel.id().asLongText()));
            channel.writeAndFlush(new PongWebSocketFrame(webSocketFrame.content().retain()));
            return;
        }
        // endregion
        // region 纯文本消息
        if (webSocketFrame instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) webSocketFrame).text();

//            LOGGER.info("├ [{} 接收到客户端的消息]: {}", new Date(), text);
//                channelHandlerContext.fireUserEventTriggered(text);

            channelHandlerContext.fireChannelRead(text);
            channelHandlerContext.fireChannelReadComplete();
            return;
        }
        // endregion
        // region 二进制消息 此处使用了MessagePack编解码方式
        if (webSocketFrame instanceof BinaryWebSocketFrame) {
            webSocketFrame.retain();//Unsupport binary data type
//                BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) webSocketFrame;
//                ByteBuf content = binaryWebSocketFrame.content();
//                LOGGER.info("├ [二进制数据]:{}", content);
//                final int length = content.readableBytes();
//                final byte[] array = new byte[length];
//                content.getBytes(content.readerIndex(), array, 0, length);
//                MessagePack messagePack = new MessagePack();
//                WebSocketMessageEntity webSocketMessageEntity = messagePack.read(array, WebSocketMessageEntity.class);
//                LOGGER.info("├ [解码数据]: {}", webSocketMessageEntity);
//                WebSocketUsers.sendMessageToUser(webSocketMessageEntity.getAcceptName(), webSocketMessageEntity.getContent());
            return;
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            switch (((IdleStateEvent) evt).state()) {
                case READER_IDLE:
                    ctx.close();
                    break;
            }
        }

    }
}
