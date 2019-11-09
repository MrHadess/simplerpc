/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service.listener;

import com.mh.simplerpc.common.ChannelReadListener;
import com.mh.simplerpc.common.ConnectionsToContext;
import com.mh.simplerpc.common.ServiceAuthHandler;
import com.mh.simplerpc.dto.AcceptInfo;
import com.mh.simplerpc.service.ServiceControl;
import com.mh.simplerpc.service.ServiceMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GenericProgressiveFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoopListenerStartup implements ServiceControl {

    private static Logger logger = LoggerFactory.getLogger(LoopListenerStartup.class);

    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private int listenerPort;

    private ConnectionsToContext connectionsToContext;

    private ServiceAuthHandler serviceAuthHandler;


//    private ChannelReadListener channelReadListener;

//    @Deprecated
//    public LoopListenerStartup(int listenerPort, ConnectionsToContext connectionsToContext, ChannelReadListener channelReadListener) {
//        this.listenerPort = listenerPort;
//        this.connectionsToContext = connectionsToContext;
//        this.channelReadListener = channelReadListener;
//    }

    public LoopListenerStartup(int listenerPort,String oauthCode,ServiceMessage serviceMessage) {
        this.listenerPort = listenerPort;

        this.connectionsToContext = new ConnectionsToContext();

        serviceAuthHandler = new ServiceAuthHandler(false,oauthCode,serviceMessage,connectionsToContext);
        connectionsToContext.adapterLinked.addLast(serviceAuthHandler);
    }


//    private ChannelReadListener channelReadListenerImpl = new ChannelReadListener() {
//        public void channelOnRead(String channelID, Object o) {
//
//        }
//    };


    public void startListener() {

//        WebSocketChannelInitializer webSocketChannelInitializer = new WebSocketChannelInitializer(
//                channelReadListener,
//                AcceptInfo.class,
//                connectionsToContext
//        );

        WebSocketChannelInitializer webSocketChannelInitializer = new WebSocketChannelInitializer(
                serviceAuthHandler,
                AcceptInfo.class,
                connectionsToContext
        );


        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler((LogLevel.INFO)))
                    .childHandler(webSocketChannelInitializer);

//            ChannelFuture channelFuture = serverBootstrap.bind(listenerPort).sync();
            ChannelFuture channelFuture = serverBootstrap.bind(listenerPort);
//            channelFuture.addListener(new ChannelFutureListener() {
//                public void operationComplete(ChannelFuture channelFuture) throws Exception {
//                    if (!channelFuture.isSuccess()) {
//                        logger.info(String.format("Connect fail (id:%s)",channelFuture.channel().id().asLongText()));
////                        System.out.println(String.format("Fail to id:%s",channelFuture.channel().id().asLongText()));
//                    }
//
//                }
//            });



            channelFuture.channel().closeFuture().sync();


        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }

    public void stopListener() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }


    public void moveMsgAcceptObject(ChannelReadListener<AcceptInfo> acceptInfoChannelReadListener) {

    }
}
