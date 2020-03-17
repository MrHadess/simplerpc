package com.mh.simplerpc.demo;

import com.mh.simplerpc.ServiceConfig;
import com.mh.simplerpc.ServiceManager;
import com.mh.simplerpc.config.ProviderEntity;

import java.util.ArrayList;


public class SampleServer {

    public static void main(String[] args) {
        new SampleServer().runServer();
    }

    public void runServer() {
        ArrayList<ProviderEntity> list = new ArrayList<ProviderEntity>();

        // Register provider entity
        ProviderEntity providerEntity = new ProviderEntity(Hello.class,HelloImpl.class);
        list.add(providerEntity);

        // And then you can register consumer entity

        ServiceConfig serviceConfig = new ServiceConfig.Builder()
                .setJobMode(ServiceConfig.JOB_MODE_LISTENER)
                .setProviderConfigList(list)
                .build();

        ServiceManager serviceManager = new ServiceManager(serviceConfig);
        serviceManager.startup();

        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Thread shutdown");

    }



}
