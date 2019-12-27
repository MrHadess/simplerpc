/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.common;

import com.google.gson.stream.JsonToken;
import com.mh.simplerpc.ServiceManager;
import com.mh.simplerpc.dto.AcceptInfo;
import com.mh.simplerpc.dto.AuthResult;
import com.mh.simplerpc.dto.ClientAuthInfo;
import com.mh.simplerpc.dto.CommunicationTypeEnum;
import com.mh.simplerpc.service.CommunicationManager;
import com.mh.simplerpc.service.ServiceControl;
import com.mh.simplerpc.service.ServiceMessage;
import com.mh.simplerpc.util.CheckCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;

public class ServiceAuthHandler implements ChannelReadListener<AcceptInfo>,ConnectionsToContext.ChannelConnectionStateListener,ServiceControl {

    private static Logger logger = LoggerFactory.getLogger(ServiceAuthHandler.class);

    private String authCode;
    private ConnectionsToContext connectionsToContext;
    private AuthStateListener authStateListener;
    private ServiceMessage serviceMessage;

    private ChannelReadListener<AcceptInfo> entrustChannelReadListener;

    private HashMap<String,String> clientAuthCodeMap = new HashMap<String, String>();// <channelID,authCode>
    private HashSet<String> authConnectID = new HashSet<String>();
    private HashSet<String> authenticating = new HashSet<String>();

    private int serviceType = -1;// 0 client,1 server
    private static final int SERVICE_TYPE_CLIENT = 0;
    private static final int SERVICE_TYPE_SERVER = 1;

    // TODO simple this code
    public ServiceAuthHandler(
            boolean isClient,
            String authCode,
            AuthStateListener authStateListener,
            ServiceMessage serviceMessage,
            ConnectionsToContext connectionsToContext
    ) {
        if (isClient) {
            serviceType = SERVICE_TYPE_CLIENT;
        } else {
            serviceType = SERVICE_TYPE_SERVER;
        }
        this.authCode = authCode;
        this.authStateListener = authStateListener;
        this.serviceMessage = serviceMessage;
        this.connectionsToContext = connectionsToContext;
        this.entrustChannelReadListener = defaultEntrustChannelReadListener;
    }

    public ServiceAuthHandler(
            boolean isClient,
            String authCode,
            ServiceMessage serviceMessage,
            ConnectionsToContext connectionsToContext
    ) {
        if (isClient) {
            serviceType = SERVICE_TYPE_CLIENT;
        } else {
            serviceType = SERVICE_TYPE_SERVER;
        }
        this.authCode = authCode;
        this.serviceMessage = serviceMessage;
        this.connectionsToContext = connectionsToContext;
        this.entrustChannelReadListener = defaultEntrustChannelReadListener;
    }

    public void channelOnRead(String channelID, AcceptInfo acceptInfo) {
        logger.debug("accept data");
        if (acceptInfo.getType() == null) return;
        switch (acceptInfo.getType()) {
            case StartAuthConnection:
                TrySendAuthCodeForClient(channelID);
                break;
            case AuthCheckCode:
                clientSignAuthCode(channelID, acceptInfo);
                break;
            case AuthResult:
                startClientAuthJob(channelID, acceptInfo);
                break;
            case ClientAuth:
                startServerAuthJob(channelID, acceptInfo);
                break;
            default:
                if (!authConnectID.contains(channelID)) {
                    defaultEntrustChannelReadListener.channelOnRead(channelID, acceptInfo);
                    break;
                }
                // entrust to job
                entrustChannelReadListener.channelOnRead(channelID, acceptInfo);
                break;
        }
    }

    private void TrySendAuthCodeForClient(String channelID) {
        if (serviceType != SERVICE_TYPE_SERVER) return;
        ChannelHandlerContext channelHandlerContext = connectionsToContext.getChannelHandlerContext(channelID);
        String checkCode = CheckCode.createCode();
        clientAuthCodeMap.put(channelID,checkCode);

        ClientAuthInfo clientAuthInfo = new ClientAuthInfo();
        clientAuthInfo.setAuthCode(checkCode);

        AcceptInfo sendAcceptInfo = new AcceptInfo();
        sendAcceptInfo.setType(CommunicationTypeEnum.AuthCheckCode);
        sendAcceptInfo.setData(clientAuthInfo);
        channelHandlerContext.channel().writeAndFlush(sendAcceptInfo);
    }

    private void clientSignAuthCode(final String channelID, AcceptInfo acceptInfo) {
        if (serviceType != SERVICE_TYPE_CLIENT) return;
        logger.debug("startClientAuthJob", acceptInfo);
        ChannelHandlerContext channelHandlerContext = connectionsToContext.getChannelHandlerContext(channelID);

        ClientAuthInfo serClientAuthInfo = ServiceManager.getGson().fromJson(acceptInfo.getData(), ClientAuthInfo.class);
        String signData = serClientAuthInfo.getAuthCode();
        signData = CheckCode.signData(signData, authCode);
        ClientAuthInfo clientAuthInfo = new ClientAuthInfo();
        clientAuthInfo.setAuthCode(signData);

        AcceptInfo sendAcceptInfo = new AcceptInfo();
        sendAcceptInfo.setType(CommunicationTypeEnum.ClientAuth);
        sendAcceptInfo.setData(clientAuthInfo);

        channelHandlerContext.channel().writeAndFlush(sendAcceptInfo);
    }

    private void startClientAuthJob(final String channelID, AcceptInfo acceptInfo) {
        if (serviceType != SERVICE_TYPE_CLIENT) return;
        logger.debug("startClientAuthJob",acceptInfo);
        ChannelHandlerContext channelHandlerContext = connectionsToContext.getChannelHandlerContext(channelID);

        AuthResult authResult = ServiceManager.getGson().fromJson(acceptInfo.getData(),AuthResult.class);
        if (!authResult.isAuthSuccess()) {
            logger.info(String.format("Auth fail for client --channelID:%s",channelID));
            return;
        }
        // remove auth success channelID
        authenticating.remove(channelID);
        // auth success then do connect success
        authConnectID.add(channelID);
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
        if (serviceType != SERVICE_TYPE_SERVER) return;
        ChannelHandlerContext channelHandlerContext = connectionsToContext.getChannelHandlerContext(channelID);
        String checkCodeForClient = clientAuthCodeMap.get(channelID);

        AcceptInfo sendAcceptInfo = new AcceptInfo();
        AuthResult authResult = new AuthResult();

        sendAcceptInfo.setType(CommunicationTypeEnum.AuthResult);

        if (checkCodeForClient == null) {
            authResult.setAuthSuccess(false);
            sendAcceptInfo.setData(authResult);
            channelHandlerContext.channel().writeAndFlush(sendAcceptInfo);
            return;
        }

        ClientAuthInfo clientAuthInfo = ServiceManager.getGson().fromJson(acceptInfo.getData(),ClientAuthInfo.class);
        if (!CheckCode.signData(checkCodeForClient,authCode).equals(clientAuthInfo.getAuthCode())) {
            logger.info(String.format("Auth fail for client --channelID:%s",channelID));

            authResult.setAuthSuccess(false);
            sendAcceptInfo.setData(authResult);
            channelHandlerContext.channel().writeAndFlush(sendAcceptInfo);

            channelHandlerContext.channel().close();
            return;
        }
        // remove auth success channelID
        authenticating.remove(channelID);
        // auth success then do connect success
        authConnectID.add(channelID);

        authResult.setAuthSuccess(true);
        sendAcceptInfo.setData(authResult);
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
        if (authStateListener != null) {
            ChannelHandlerContext ctx = connectionsToContext.getChannelHandlerContext(channelID);

            if (authConnectID.contains(channelID)) {
                authStateListener.authSuccessDisconnect(
                        ctx,
                        (InetSocketAddress) ctx.channel().localAddress(),
                        (InetSocketAddress) ctx.channel().remoteAddress()
                );
            } else if (authenticating.contains(channelID)) {
                authStateListener.connectDisconnectAndIncompleteAuth(
                        ctx,
                        (InetSocketAddress) ctx.channel().localAddress(),
                        (InetSocketAddress) ctx.channel().remoteAddress()
                );
            }
        }

        clientAuthCodeMap.remove(channelID);
        authConnectID.remove(channelID);
        authenticating.remove(channelID);
        serviceMessage.disconnect(channelID);

    }

    public void connect(String channelID) {
        if (serviceType == SERVICE_TYPE_SERVER) {
            logger.info("has new client connect",channelID);
        }
        authenticating.add(channelID);
    }
}
