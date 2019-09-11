package com.mh.simplerpc.common;

import com.mh.simplerpc.dto.AcceptInfo;
import com.mh.simplerpc.dto.CommunicationTypeEnum;
import com.mh.simplerpc.service.ServiceControl;
import com.mh.simplerpc.service.ServiceMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;

public class ServiceAuthHandler implements ChannelReadListener<AcceptInfo>,ConnectionsToContext.ChannelConnectionStateListener,ServiceControl {

    private static Logger logger = LoggerFactory.getLogger(ServiceAuthHandler.class);

    private ConnectionsToContext connectionsToContext;
    private AuthStateListener authStateListener;
    private ServiceMessage serviceMessage;

    private ChannelReadListener<AcceptInfo> entrustChannelReadListener;

    private HashSet<String> authConnectID = new HashSet<String>();

    private int serviceType = -1;// 0 client,1 server


    // TODO simple this code
    public ServiceAuthHandler(boolean isClient,AuthStateListener authStateListener,ServiceMessage serviceMessage, ConnectionsToContext connectionsToContext) {
        if (isClient) {
            serviceType = 0;
        } else {
            serviceType = 1;
        }
        this.authStateListener = authStateListener;
        this.serviceMessage = serviceMessage;
        this.connectionsToContext = connectionsToContext;
        this.entrustChannelReadListener = defaultEntrustChannelReadListener;
    }

    public ServiceAuthHandler(boolean isClient,ServiceMessage serviceMessage, ConnectionsToContext connectionsToContext) {
        if (isClient) {
            serviceType = 0;
        } else {
            serviceType = 1;
        }
        this.serviceMessage = serviceMessage;
        this.connectionsToContext = connectionsToContext;
        this.entrustChannelReadListener = defaultEntrustChannelReadListener;
    }

    public void channelOnRead(String channelID, AcceptInfo acceptInfo) {
        logger.debug("accept data");
        if (acceptInfo.getType() == null) return;
        switch (acceptInfo.getType()) {
            case AuthResult:
            case ClientAuth:
                switch (serviceType) {
                    case 0:
                        startClientAuthJob(channelID,acceptInfo);
                        break;
                    case 1:
                        startServerAuthJob(channelID,acceptInfo);
                        break;
                }

                break;
            default:
                if (!authConnectID.contains(channelID)) {
                    defaultEntrustChannelReadListener.channelOnRead(channelID,acceptInfo);
                    break;
                }
                // entrust to job
                entrustChannelReadListener.channelOnRead(channelID,acceptInfo);
                break;
        }
    }

    private void startClientAuthJob(final String channelID, AcceptInfo acceptInfo) {
        logger.debug("startClientAuthJob",acceptInfo);
        if (acceptInfo.getType() != CommunicationTypeEnum.AuthResult) return;
        // auth success then do connect success
        authConnectID.add(channelID);
        ChannelHandlerContext channelHandlerContext = connectionsToContext.getChannelHandlerContext(channelID);
        boolean connectState = serviceMessage.connectSuccess(this,channelHandlerContext);
        // start ping frame
        new Thread(new Runnable() {
            public void run() {
                ChannelHandlerContext channelHandlerContext = connectionsToContext.getChannelHandlerContext(channelID);
//                channelHandlerContext.channel().pipeline().get()
                PingWebSocketFrame pingWebSocketFrame = new PingWebSocketFrame();
                while (authConnectID.contains(channelID)) {
                    logger.debug(String.format("Try send ping frame to channel id:%s",channelID));
                    pingWebSocketFrame.retain();
                    channelHandlerContext.channel().writeAndFlush(pingWebSocketFrame);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void startServerAuthJob(String channelID, AcceptInfo acceptInfo) {
        if (acceptInfo.getType() != CommunicationTypeEnum.ClientAuth) return;
        // auth success then do connect success
        authConnectID.add(channelID);
        ChannelHandlerContext channelHandlerContext = connectionsToContext.getChannelHandlerContext(channelID);

        AcceptInfo sendAcceptInfo = new AcceptInfo();
        sendAcceptInfo.setType(CommunicationTypeEnum.AuthResult);
        channelHandlerContext.channel().writeAndFlush(sendAcceptInfo);

        boolean connState = serviceMessage.connectSuccess(this,channelHandlerContext);
        if (!connState) {
            channelHandlerContext.close();
        }
    }

    private ChannelReadListener<AcceptInfo> defaultEntrustChannelReadListener = new ChannelReadListener<AcceptInfo>() {
        // Default handler
        public void channelOnRead(String channelID, AcceptInfo o) {
            ChannelHandlerContext context = connectionsToContext.getChannelHandlerContext(channelID);

            AcceptInfo sendAcceptInfo = new AcceptInfo();
            sendAcceptInfo.setType(CommunicationTypeEnum.RequestReject);
            context.channel().writeAndFlush(sendAcceptInfo);
        }
    };


    public void moveMsgAcceptObject(ChannelReadListener<AcceptInfo> acceptInfoChannelReadListener) {
        if (acceptInfoChannelReadListener == null) return;
        entrustChannelReadListener = acceptInfoChannelReadListener;
    }

    public void disconnect(String channelID) {
        logger.info(String.format("disconnect %s",channelID));

        // has register listener and auth success connect,just do it
        if (authStateListener != null && authConnectID.contains(channelID)) {
            ChannelHandlerContext ctx = connectionsToContext.getChannelHandlerContext(channelID);
            authStateListener.authSuccessDisconnect(
                    ctx,
                    (InetSocketAddress) ctx.channel().localAddress(),
                    (InetSocketAddress) ctx.channel().remoteAddress()
            );
        }

        authConnectID.remove(channelID);
        serviceMessage.disconnect(channelID);

    }

    public void connect(String channelID) {
        if (serviceType == 1) {
            logger.info("has new client connect",channelID);
        }
    }
}
