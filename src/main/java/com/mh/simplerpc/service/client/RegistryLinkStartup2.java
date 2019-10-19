/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service.client;

import com.mh.simplerpc.common.AuthStateListener;
import com.mh.simplerpc.common.ConnectionsToContext;
import com.mh.simplerpc.common.ServiceAuthHandler;
import com.mh.simplerpc.dto.AcceptInfo;
import com.mh.simplerpc.dto.CommunicationTypeEnum;
import com.mh.simplerpc.service.ServiceMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RegistryLinkStartup2 implements AuthStateListener {

    private EventLoopGroup group = new NioEventLoopGroup();

    private String accessIpAdder;
    private int accessPort;
    private ServiceMessage serviceMessage;
    private int tryConnectNum;
    private int tryRecoveryConnectNum;
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(100);

    private static Logger logger = LoggerFactory.getLogger(RegistryLinkStartup2.class);

    // TODO simple this code
    public RegistryLinkStartup2(String accessIpAdder, int accessPort, ServiceMessage serviceMessage,int tryConnectNum,int tryRecoveryConnectNum) {
        this.accessIpAdder = accessIpAdder;
        this.accessPort = accessPort;
        this.serviceMessage = serviceMessage;
        this.tryConnectNum = tryConnectNum;
        this.tryRecoveryConnectNum = tryRecoveryConnectNum;
//        serviceAuthHandler = new ServiceAuthHandler(true,serviceMessage,connectionsToContext);
        serviceAuthHandler = new ServiceAuthHandler(true,this,serviceMessage,connectionsToContext);
        connectionsToContext.adapterLinked.addLast(serviceAuthHandler);
        connectionsToContext.adapterLinked.addLast(new ConnectionsToContext.ChannelConnectionStateListener() {
            public void disconnect(String channelID) {
                logger.info(String.format("ClientDisconnectServer:%s",channelID));
            }

            public void connect(final String channelID) {
                logger.info(String.format("ClientConnectionServer:%s",channelID));
//                new Thread(new HandShakerFirstAction(channelID)).start();
            }
        });
    }

    private ConnectionsToContext connectionsToContext = new ConnectionsToContext();

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

    public void authSuccessDisconnect(ChannelHandlerContext channelHandlerContext, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {

        logger.info("link connect and auth success, now is disconnect,try connect again");
//        logger.info(remoteAddress.getAddress().toString());// this address prefix is '/'
//        logger.info(remoteAddress.getHostName());// if link adder is '127.0.0.1' ,then host is not match
//        logger.info(String.valueOf(remoteAddress.getPort()));
//        startLink(remoteAddress.getHostName(),remoteAddress.getPort());

//        TryConnectListener tryConnectListener = new TryConnectListener(accessIpAdder,accessPort);
//        tryConnectListener.setAuthConnectRecovery(true);
//        startLink(tryConnectListener);

        scheduledExecutorService.schedule(new Runnable() {
            public void run() {
                TryConnectListener tryConnectListener = new TryConnectListener(accessIpAdder,accessPort);
                tryConnectListener.setAuthConnectRecovery(true);
                startLink(tryConnectListener);
            }
        },10,TimeUnit.SECONDS);
    }

    public void startLink() {
        startLink(new TryConnectListener(accessIpAdder,accessPort));
    }

    public void startLink(ChannelFutureListener reconnectListener) {
        group = new NioEventLoopGroup();
        URI connectURI = null;
        try {
            connectURI = new URI(String.format("ws://%s:%s/",accessIpAdder,accessPort));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        WebSocketChannelInitializer webSocketChannelInitializer = new WebSocketChannelInitializer(
                connectURI,
                serviceAuthHandler,
                AcceptInfo.class,
                connectionsToContext
        );

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .channel(NioSocketChannel.class)
                    .handler(webSocketChannelInitializer);


//            ChannelFuture channelFuture = bootstrap.connect(accessIpAdder,accessPort).sync();
//            channelFuture.addListener(new ChannelFutureListener(){
//                public void operationComplete(final ChannelFuture channelFuture) throws Exception {
//                    if (channelFuture.isSuccess()) {
//                        new Thread(new HandShakerFirstAction(channelFuture.channel().id().asLongText())).start();
////                        channelFuture.channel().closeFuture().sync();
//                        channelFuture.channel().closeFuture();
//                    } else {
//                        channelFuture.channel().eventLoop().schedule(new Runnable() {
//                            public void run() {
//                                startLink(accessIpAdder,accessPort);
//                            }
//                        },10,TimeUnit.SECONDS);
//                    }
//                }
//            });

            logger.info(String.format("try connect client %s:%s",accessIpAdder,accessPort));
            ChannelFuture channelFuture = bootstrap.connect(accessIpAdder,accessPort);
            channelFuture.addListener(reconnectListener);



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class TryConnectListener implements ChannelFutureListener {

        private String accessIpAdder;
        private int accessPort;
        private boolean authConnectRecovery = false;
        private int nowTryConnectNum = 0;

        public TryConnectListener(String accessIpAdder, int accessPort) {
            this.accessIpAdder = accessIpAdder;
            this.accessPort = accessPort;
        }

        private void setAuthConnectRecovery(boolean state) {
            this.authConnectRecovery = state;
        }

        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (channelFuture.isSuccess()) {
                HandShakerFirstAction handShakerFirstAction = new HandShakerFirstAction(
                        channelFuture.channel().id().asLongText(),
                        TryConnectListener.this
                );
                new Thread(handShakerFirstAction).start();
//                new Thread(new HandShakerFirstAction(channelFuture.channel().id().asLongText())).start();
//                channelFuture.channel().closeFuture().sync();
                channelFuture.channel().closeFuture();
                return;
            }

            nowTryConnectNum++;
            if (authConnectRecovery) {
                if (nowTryConnectNum > tryRecoveryConnectNum && tryRecoveryConnectNum != -1) {
                    logger.info(String.format("Try recovery auth connect value full.un try again---%s:%s", accessIpAdder, accessPort));
                    return;
                }
            } else {
                if (nowTryConnectNum > tryConnectNum && tryConnectNum != -1) {
                    logger.info(String.format("Try connect value full.un try again---%s:%s", accessIpAdder, accessPort));
                    return;
                }
            }

            logger.info(String.format("connect fail---%s:%s", accessIpAdder, accessPort));
//            // NioEventLoopGroup => group will be 'null',this code will be not work
//            channelFuture.channel().eventLoop().schedule(new Runnable() {
//                public void run() {
//                    startLink(TryConnectListener.this);
//                }
//            }, 10, TimeUnit.SECONDS);

            scheduledExecutorService.schedule(new Runnable() {
                public void run() {
                    startLink(TryConnectListener.this);
                }
            },10,TimeUnit.SECONDS);

        }
    }

    public void cutLink() {
        group.shutdownGracefully();
    }

    class HandShakerFirstAction implements Runnable {

        private static final int WeakTime = 1000 * 60 * 1;// Max wait time 1min
        private static final int WeakNum = WeakTime / 10;
        private String channelID;
        private ChannelFutureListener reconnectListener;

        public HandShakerFirstAction(String channelID,ChannelFutureListener reconnectListener) {
            this.channelID = channelID;
            this.reconnectListener = reconnectListener;
        }

        public void run() {
            ChannelHandlerContext ctx = connectionsToContext.getChannelHandlerContext(channelID);
            WebSocketClientProtocolHandler w= (WebSocketClientProtocolHandler) ctx.pipeline().get("WebSocketClientProtocolHandler");
            WebSocketClientHandshaker webSocketClientHandshaker = w.handshaker();

            int waitNum = 0;
            while (!webSocketClientHandshaker.isHandshakeComplete() && waitNum < WeakNum) {

                waitNum++;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

//            while (true) {
//                if (ctx.isRemoved()) break;
//                if (!webSocketClientHandshaker.isHandshakeComplete() && waitNum < WeakNum) break;
//
//
//                waitNum++;
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//            }
            if (waitNum >= WeakNum){
                logger.info("Hand shaker fail -- disconnect ...");
//                ctx.close();//握手超时 断开连接
                ctx.disconnect();
                scheduledExecutorService.schedule(new Runnable() {
                    public void run() {
                        startLink(reconnectListener);
                    }
                },10,TimeUnit.SECONDS);
                return;
            }

            if (handShakerStateListener != null) {
                handShakerStateListener.success(channelID);
            }

            System.out.println(String.format("ClientSendMSGSuccess:%s,%s", "ClientHandShakerSuccess",waitNum*10));
        }
    }



}
