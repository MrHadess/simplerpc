package com.mh.simplerpc.service.listener;

import com.mh.simplerpc.common.*;
import com.mh.simplerpc.common.WebSocketFrameHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;


public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    private ChannelReadListener channelReadListener;
    private Class<?> serializableClass;
    private ConnectionsToContext connectionsToContext;

//    public WebSocketChannelInitializer(ChannelReadListener channelReadListener, Class<?> serializableClass) {
//        this.channelReadListener = channelReadListener;
//        this.serializableClass = serializableClass;
//    }

    public WebSocketChannelInitializer(ChannelReadListener channelReadListener, Class<?> serializableClass, ConnectionsToContext connectionsToContext) {
        this.channelReadListener = channelReadListener;
        this.serializableClass = serializableClass;
        this.connectionsToContext = connectionsToContext;

    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        // client will be 10sec heartbeat.read time cut connect timeout is 15sec,else 'write' 'all' will be 5min close connect
        pipeline.addLast("IdleStateHandler",new IdleStateHandler(15,300,300));


        pipeline.addLast("HttpServerCodec", new HttpServerCodec());
        pipeline.addLast("ChunkedWriteHandler", new ChunkedWriteHandler());

        /*
        * 作用是将一个Http的消息组装成一个完成的HttpRequest或者HttpResponse，那么具体的是什么
        * 取决于是请求还是响应, 该Handler必须放在HttpServerCodec后的后面
         */
        pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(8192));//Max connect value

        //用于处理websocket, '/'为访问websocket时的uri
//        pipeline.addLast("WebSocketServerProtocolHandler", new WebSocketServerProtocolHandler("/"));
//        WebSocketServerProtocolHandler(String websocketPath, String subprotocols, boolean allowExtensions, int maxFrameSize)
        pipeline.addLast("WebSocketServerProtocolHandler", new WebSocketServerProtocolHandler("/",null,false,2048 * 100000));
        pipeline.addLast("ConnectStateAdapter",new ConnectionsToContext.ConnectionAdapter(connectionsToContext));
        pipeline.addLast("WebSocketFrameHandler",new WebSocketFrameHandler());

        pipeline.addLast("ObjectInDataHandler", new ObjectInDataHandler(serializableClass,channelReadListener));
        pipeline.addLast("ObjectOutDataHandler",new ObjectOutDataHandler());




    }
}
