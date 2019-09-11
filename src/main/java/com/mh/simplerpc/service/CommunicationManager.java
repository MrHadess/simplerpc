package com.mh.simplerpc.service;

import com.mh.simplerpc.ServiceConfig;
import com.mh.simplerpc.common.ChannelReadListener;
import com.mh.simplerpc.common.ConnectionsToContext;
import com.mh.simplerpc.dto.AcceptInfo;
import com.mh.simplerpc.service.client.RegistryLinkStartup;
import com.mh.simplerpc.service.client.RegistryLinkStartup2;
import com.mh.simplerpc.service.listener.LoopListenerStartup;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommunicationManager {

    private ServiceConfig serviceConfig;

    // 0 unconnected,1 connect success
    private int connectState = 0;
    private ProcessHandler processHandler;

    private ExecutorService runGroupService = Executors.newCachedThreadPool();

    private LoopListenerStartup loopListenerStartup;
//    private RegistryLinkStartup registryLinkStartup;
    private RegistryLinkStartup2 registryLinkStartup;

    @Deprecated
    public CommunicationManager(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
        processHandler = new ProcessHandler(serviceConfig);
    }

    public CommunicationManager(ServiceConfig serviceConfig,ProcessHandler processHandler) {
        this.serviceConfig = serviceConfig;
        this.processHandler = processHandler;
    }

    public synchronized void startup() {
        int jobMode = serviceConfig.getJobMode();
        final String remoteIP = serviceConfig.getRemoteIP();
        final int port = serviceConfig.getRemotePort();

        /// TODO bug fix if jobMode==0 start loop before will be block thread

        if (jobMode == 0 || jobMode == 1) {
            if (loopListenerStartup == null) {
                loopListenerStartup = new LoopListenerStartup(port, serviceMessage);
            }
            runGroupService.execute(new Runnable() {
                public void run() {
                    loopListenerStartup.startListener();
                }
            });
        }

        if (jobMode == 0 || jobMode == 2) {
            if (registryLinkStartup == null) {
//                registryLinkStartup = new RegistryLinkStartup(serviceMessage);
                registryLinkStartup = new RegistryLinkStartup2(
                        remoteIP,
                        port,
                        serviceMessage,
                        serviceConfig.getTryConnectNum(),
                        serviceConfig.getTryRecoveryConnectNum()
                );
            }
            runGroupService.execute(new Runnable() {
                public void run() {
//                    registryLinkStartup.startLink(remoteIP, port);
                    registryLinkStartup.startLink();
                }
            });
        }



    }

    public void shutdown() {
        if (registryLinkStartup != null) {
            registryLinkStartup.cutLink();
        }
        if (loopListenerStartup != null) {
            loopListenerStartup.stopListener();
        }
    }

    private ServiceMessage serviceMessage = new ServiceMessage() {
        public synchronized boolean connectSuccess(ServiceControl serviceControl, ChannelHandlerContext channelHandlerContext) {
            if (connectState == 1) return false;
            connectState = 1;
            processHandler.setChannelHandlerContext(channelHandlerContext);
            serviceControl.moveMsgAcceptObject(processHandler);
            processHandler.authSuccess();
            return true;
        }

        public void disconnect(String channelID) {
            if (processHandler.tryRecoveryContext(channelID)) {
                connectState = 0;
            }
        }
    };



}
