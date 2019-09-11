/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service.client;

import com.mh.simplerpc.common.ServiceAuthHandler;
import com.mh.simplerpc.common.ConnectionsToContext;
import com.mh.simplerpc.dto.AcceptInfo;
import com.mh.simplerpc.dto.CommunicationTypeEnum;
import com.mh.simplerpc.service.ServiceMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GenericProgressiveFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

@Deprecated
//public class RegistryLinkStartup implements ServiceControl {
public class RegistryLinkStartup {

    private EventLoopGroup group = new NioEventLoopGroup();
    private ServiceMessage serviceMessage;

    private static Logger logger = LoggerFactory.getLogger(RegistryLinkStartup.class);

    public RegistryLinkStartup(ServiceMessage serviceMessage) {
        this.serviceMessage = serviceMessage;
        serviceAuthHandler = new ServiceAuthHandler(true,serviceMessage,connectionsToContext);
        connectionsToContext.adapterLinked.addLast(serviceAuthHandler);
    }

    private ConnectionsToContext connectionsToContext = new ConnectionsToContext();

//    private ChannelReadListener channelReadListener = new ChannelReadListener<AcceptInfo>() {
//        public void channelOnRead(String channelID, AcceptInfo acceptInfo) {
//            if (acceptInfo.getType() == null) return;
//            ChannelHandlerContext channelHandlerContext = connectionsToContext.getChannelHandlerContext(channelID);
//            switch (acceptInfo.getType()) {
//                case AuthResult:
//                    // auth success then do connect success
//                    serviceMessage.connectSuccess(RegistryLinkStartup.this,channelHandlerContext);
//                    break;
//                case ClientAuth:
//                    break;
//                default:
//                    // entrust to job
//                    entrustChannelReadListener.channelOnRead(channelID,acceptInfo);
//                    break;
//            }
//        }
//    };
//
//    private ChannelReadListener<AcceptInfo> entrustChannelReadListener = new ChannelReadListener<AcceptInfo>() {
//        // Default handler
//        public void channelOnRead(String channelID, AcceptInfo o) {
//            ChannelHandlerContext context = connectionsToContext.getChannelHandlerContext(channelID);
//
//            AcceptInfo sendAcceptInfo = new AcceptInfo();
//            sendAcceptInfo.setType(CommunicationTypeEnum.RequestReject);
//            context.channel().writeAndFlush(sendAcceptInfo);
//        }
//    };
//    public void moveMsgAcceptObject(ChannelReadListener<AcceptInfo> acceptInfoChannelReadListener) {
//        if (acceptInfoChannelReadListener == null) return;
//        this.entrustChannelReadListener = acceptInfoChannelReadListener;
//
//    }
//    public void moveMsgAcceptObject(ChannelReadListener<AcceptInfo> acceptInfoChannelReadListener) {
//        clientAuthHandler.moveMsgAcceptObject(acceptInfoChannelReadListener);
//    }

    private ServiceAuthHandler serviceAuthHandler;

    public interface HandShakerStateListener {
        void success(String channelID);
    }

    private HandShakerStateListener handShakerStateListener = new HandShakerStateListener() {
        public void success(String channelID) {
            ChannelHandlerContext channelHandlerContext = connectionsToContext.getChannelHandlerContext(channelID);
            if (channelHandlerContext == null) {
                logger.error(String.format("Client shaker success,after get context unable(id:%s)",channelID));
                return;
            }
            AcceptInfo sendAcceptInfo = new AcceptInfo();
            sendAcceptInfo.setType(CommunicationTypeEnum.ClientAuth);
            channelHandlerContext.channel().writeAndFlush(sendAcceptInfo);
        }
    };


    public void startLink(final String accessIpAdder, final int accessPort) {

        connectionsToContext.adapterLinked.addLast(new ConnectionsToContext.ChannelConnectionStateListener() {
            public void disconnect(String channelID) {
                logger.info(String.format("ClientDisconnectServer:%s",channelID));
//                System.out.println(String.format("ClientDisconnectServer:%s",channelID));
            }

            public void connect(final String channelID) {
                logger.info(String.format("ClientConnectionServer:%s",channelID));
//                System.out.println(String.format("ClientConnectionServer:%s",channelID));
                new Thread(new HandShakerFirstAction(channelID)).start();
            }
        });


        URI connectURI = null;
        try {
            connectURI = new URI(String.format("ws://%s:%s/",accessIpAdder,accessPort));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
//        WebSocketChannelInitializer webSocketChannelInitializer = new WebSocketChannelInitializer(
//                connectURI,
//                channelReadListener,
//                AcceptInfo.class,
//                connectionsToContext
//        );

        WebSocketChannelInitializer webSocketChannelInitializer = new WebSocketChannelInitializer(
                connectURI,
                serviceAuthHandler,
                AcceptInfo.class,
                connectionsToContext
        );

        try {
            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .channel(NioSocketChannel.class)
                    .handler(webSocketChannelInitializer);


//            ChannelFuture channelFuture = bootstrap.connect(accessIpAdder,accessPort).sync();
//            channelFuture.channel().closeFuture().sync();

            logger.info("Try connect");
            ChannelFuture channelFuture = bootstrap.connect(accessIpAdder,accessPort);
            channelFuture.channel().closeFuture().sync();


//            WebSocketClientHandshaker handler = (WebSocketClientHandshaker)channelFuture.channel().pipeline().get("hookedHandler");
//            handler.setHandshaker(handshaker);
//            handshaker.handshake(channel);
//            //阻塞等待是否握手成功
//            handler.handshakeFuture().sync();

//            Channel channel = channelFuture.channel();
//            channel.write(new TextWebSocketFrame("{\"data\":\"message1\"}"));
//            channel.flush();

//            URI websocketURI = new URI("ws://127.0.0.1:28095/");
//            HttpHeaders httpHeaders = new DefaultHttpHeaders();
//            //进行握手
//            WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(websocketURI, WebSocketVersion.V13, (String)null, true,httpHeaders);
//            System.out.println("connect");
//            Channel channel=bootstrap.connect(websocketURI.getHost(),websocketURI.getPort()).sync().channel();
////            WebSocketFrameHandler httpObjectAggregator = (WebSocketFrameHandler) channel.pipeline().get("httpObjectAggregator");
//            WebSocketClientProtocolHandler httpObjectAggregator = (WebSocketClientProtocolHandler) channel.pipeline().get("WebSocketServerProtocolHandler");
//
//
//
////            httpObjectAggregator.handshaker().handshake(channel);
//            httpObjectAggregator.handshaker().handshake(channel).sync();

//            WebSocketClientHandler handler = (WebSocketClientHandler)channel.pipeline().get("hookedHandler");
//            handler.setHandshaker(handshaker);
//            handshaker.handshake(channel);
//            //阻塞等待是否握手成功
//            handler.handshakeFuture().sync();




        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }


    }

    public void cutLink() {
        group.shutdownGracefully();
    }

    class HandShakerFirstAction implements Runnable {

        private static final int WeakTime = 1000 * 60 * 2;// Max wait time 2min
        private static final int WeakNum = WeakTime / 10;
        private String channelID;

        public HandShakerFirstAction(String channelID) {
            this.channelID = channelID;
        }

        public void run() {
            ChannelHandlerContext ctx = connectionsToContext.getChannelHandlerContext(channelID);
            WebSocketClientProtocolHandler w= (WebSocketClientProtocolHandler) ctx.pipeline().get("WebSocketClientProtocolHandler");
            WebSocketClientHandshaker webSocketClientHandshaker = w.handshaker();

            int waitNum = 0;
            while (!webSocketClientHandshaker.isHandshakeComplete() && waitNum < WeakNum){
                waitNum++;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (waitNum > WeakNum){
                ctx.close();//握手超时 断开连接
                return;
            }

            if (handShakerStateListener != null) {
                handShakerStateListener.success(channelID);
            }

            System.out.println(String.format("ClientSendMSGSuccess:%s,%s", "ClientHandShakerSuccess",waitNum*10));
//            ctx.writeAndFlush(new TextWebSocketFrame("{\"data\":\"ClientChannelActive2\"}"));
        }
    }



}
