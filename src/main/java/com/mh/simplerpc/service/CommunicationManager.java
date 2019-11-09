/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service;

import com.mh.simplerpc.ServiceConfig;
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
                loopListenerStartup = new LoopListenerStartup(port,serviceConfig.getOAuthCode(),serviceMessage);
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
                        serviceConfig.getOAuthCode(),
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
            if (connectState == 0) return;
            if (processHandler.tryRecoveryContext(channelID)) {
                connectState = 0;
            }
        }
    };



}
