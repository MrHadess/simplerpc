/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service.client;

import com.mh.simplerpc.common.*;
import com.mh.simplerpc.common.WebSocketFrameHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.URI;


public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    private URI connectURI;
    private ChannelReadListener channelReadListener;
    private Class<?> serializableClass;
    private ConnectionsToContext connectionsToContext;

//    public WebSocketChannelInitializer(ChannelReadListener channelReadListener, Class<?> serializableClass) {
//        this.channelReadListener = channelReadListener;
//        this.serializableClass = serializableClass;
//    }

    public WebSocketChannelInitializer(URI connectURI, ChannelReadListener channelReadListener, Class<?> serializableClass, ConnectionsToContext connectionsToContext) {
        this.connectURI = connectURI;
        this.channelReadListener = channelReadListener;
        this.serializableClass = serializableClass;
        this.connectionsToContext = connectionsToContext;
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

//        pipeline.addFirst("LogHandler",new LoggingHandler(LogLevel.INFO));

        // intercept connect success but link unchanged to websocket agreement communication (check function)
        // client will be 10sec heartbeat.read time cut connect timeout is 15sec,else 'write' 'all' will be 5min close connect
        pipeline.addLast("IdleStateHandler",new IdleStateHandler(20,20,0));

        pipeline.addLast("HttpServerCodec", new HttpClientCodec());
        pipeline.addLast("ChunkedWriteHandler", new ChunkedWriteHandler());

        /*
        * 作用是将一个Http的消息组装成一个完成的HttpRequest或者HttpResponse，那么具体的是什么
        * 取决于是请求还是响应, 该Handler必须放在HttpServerCodec后的后面
         */
        pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(8192));//Max connect value

        //用于处理websocket, '/'为访问websocket时的uri
//        pipeline.addLast("WebSocketServerProtocolHandler", new WebSocketServerProtocolHandler("/"));
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
//        URI url = new URI("ws://127.0.0.1:28095/");
//        WebSocketClientHandshaker webSocketServerHandshaker = new WebSocketClientHandshaker13(url, WebSocketVersion.V13, null, true, httpHeaders, 2048);
//        pipeline.addLast("WebSocketServerProtocolHandler", new WebSocketClientProtocolHandler(webSocketServerHandshaker));
//        pipeline.addLast("WebSocketClientProtocolHandler", new WebSocketClientProtocolHandler(connectURI,WebSocketVersion.V13,null,true,httpHeaders,2048));
        pipeline.addLast("WebSocketClientProtocolHandler", new WebSocketClientProtocolHandler(connectURI,WebSocketVersion.V13,null,true,httpHeaders,2048 * 100000));
//        pipeline.addLast("WebSocketClientProtocolHandler", new CustomWebSocketClientProtocolHandler(url,WebSocketVersion.V13,null,true,httpHeaders,2048));
        pipeline.addLast("ConnectStateAdapter",new ConnectionsToContext.ConnectionAdapter(connectionsToContext));
        pipeline.addLast("WebSocketFrameHandler",new WebSocketFrameHandler());


        pipeline.addLast("ObjectInDataHandler", new ObjectInDataHandler(serializableClass,channelReadListener));
        pipeline.addLast("ObjectOutDataHandler",new ObjectOutDataHandler());





    }


}
