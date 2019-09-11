/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc;

import com.google.gson.Gson;
import com.mh.simplerpc.exceptions.UnknownResourceException;
import com.mh.simplerpc.service.CommunicationManager;
import com.mh.simplerpc.service.ConfigCheck;
import com.mh.simplerpc.service.ProcessHandler;
import com.mh.simplerpc.service.protocol.ProtocolCore;

public class ServiceManager {

    // full share use to json ser
    private static Gson gson = new Gson();
    public static Gson getGson() {
        return gson;
    }


    private ServiceConfig serviceConfig;
    private CommunicationManager communicationManager;
    private ProcessHandler processHandler;
    private ProtocolCore protocolCore;

    public void startup() {
        communicationManager.startup();


        // load object to memory

    }

    public void shutdown() {
        communicationManager.shutdown();
    }

    public ServiceManager(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
        init();

//        动态LOGS
//        Level level = Level.toLevel(l);
//        Logger logger = LogManager.getLogger(p);
//        logger.setLevel(level)

    }

    private void init() {
        // check service config
//        ServiceConfig tempSC = serviceConfig;
        ConfigCheck.checkIsInterface(serviceConfig.getConsumerConfigList());
        ConfigCheck.checkIsImpl(serviceConfig.getProviderConfigList());

        // 实例化 protocol
        protocolCore = new ProtocolCore(serviceConfig);

        processHandler = new ProcessHandler(serviceConfig,protocolCore);
        protocolCore.setResultControl(processHandler);
        communicationManager = new CommunicationManager(serviceConfig,processHandler);
//        communicationManager = new CommunicationManager(serviceConfig);

    }

    public <T> T getBean(Class<T> classes) throws UnknownResourceException,IllegalArgumentException {
        return protocolCore.getBean(classes);
    }

    public Object getBean(String res) throws UnknownResourceException,IllegalArgumentException {
        return protocolCore.getBean(res);
    }


}
