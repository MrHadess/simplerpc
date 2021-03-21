/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service;

import com.mh.simplerpc.ServiceConfig;
import com.mh.simplerpc.exceptions.LoadSSLEngineException;
import com.mh.simplerpc.service.client.RegistryLinkStartup2;
import com.mh.simplerpc.service.listener.LoopListenerStartup;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class CommunicationManager {

    private ServiceConfig serviceConfig;

    private final AtomicInteger connectStateAtomic = new AtomicInteger(0);
    private ProcessHandler processHandler;

    private ExecutorService runGroupService = Executors.newCachedThreadPool();

    private LoopListenerStartup loopListenerStartup;
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

    public synchronized void startup() throws LoadSSLEngineException {
        int jobMode = serviceConfig.getJobMode();
        final String remoteIP = serviceConfig.getRemoteIP();
        final int port = serviceConfig.getRemotePort();

        /// TODO bug fix if jobMode==0 start loop before will be block thread

        if (jobMode == ServiceConfig.JOB_MODE_HYBRID || jobMode == ServiceConfig.JOB_MODE_LISTENER) {
            if (loopListenerStartup == null) {
                loopListenerStartup = new LoopListenerStartup(
                        port,
                        serviceConfig.getEncryptConnectInfo(),
                        serviceConfig.getOAuthCode(),
                        serviceMessage
                );
            }
            runGroupService.execute(new Runnable() {
                public void run() {
                    loopListenerStartup.startListener();
                }
            });
        }

        if (jobMode == ServiceConfig.JOB_MODE_HYBRID || jobMode == ServiceConfig.JOB_MODE_CONNECT) {
            if (registryLinkStartup == null) {
                registryLinkStartup = new RegistryLinkStartup2.Builder()
                        .setAccessIpAdder(remoteIP)
                        .setAccessPort(port)
                        .setEncryptConnectInfo(serviceConfig.getEncryptConnectInfo())
                        .setAuthCode(serviceConfig.getOAuthCode())
                        .setServiceMessage(serviceMessage)
                        .setTryConnectNum(serviceConfig.getTryConnectNum())
                        .setTryRecoveryConnectNum(serviceConfig.getTryRecoveryConnectNum())
                        .build();
            }
            runGroupService.execute(new Runnable() {
                public void run() {
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
        public boolean connectSuccess(ServiceControl serviceControl, ChannelHandlerContext channelHandlerContext) {
            if (connectStateAtomic.get() == 1) return false;
            connectStateAtomic.set(1);
            processHandler.setChannelHandlerContext(channelHandlerContext);
            serviceControl.moveMsgAcceptObject(processHandler);
            processHandler.authSuccess();
            return true;
        }

        public void disconnect(String channelID) {
            if (connectStateAtomic.get() == 0) return;
            if (processHandler.tryRecoveryContext(channelID)) {
                connectStateAtomic.set(0);
            }
        }
    };



}
