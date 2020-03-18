package com.mh.simplerpc.demo;

import com.mh.simplerpc.ServiceConfig;
import com.mh.simplerpc.ServiceManager;
import com.mh.simplerpc.config.ConsumerEntity;
import com.mh.simplerpc.exceptions.DoesNotConnectException;

import java.util.ArrayList;


public class SampleClient {

    public static void main(String[] args) {
        new SampleClient().runClient();
    }

    private void runClient() {
        // Register consumer entity
        ArrayList<ConsumerEntity> consumerEntityList = new ArrayList<ConsumerEntity>();
        consumerEntityList.add(new ConsumerEntity(Hello.class));

        // And then you can register provider entity

        ServiceConfig serviceConfig = new ServiceConfig.Builder()
                .setRemoteIP("127.0.0.1")
                .setJobMode(ServiceConfig.JOB_MODE_CONNECT)
                .setTryRecoveryConnectNum(-1) // loop try again
                .setTryConnectNum(2) // try number 2
                .setConsumerConfigList(consumerEntityList)
                .build();

        final ServiceManager serviceManager = new ServiceManager(serviceConfig);
        serviceManager.startup();

        // Load bean proxy
        Hello helloProxy = serviceManager.getBean(Hello.class);
        // Run thread to rpc
        new Thread(new Run1(1,helloProxy)).start();

        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Thread shutdown");

    }



    class Run1 implements Runnable {
        private int remoteParamValue;
        private Hello hello;

        public Run1(int remoteParamValue, Hello helloImpl) {
            this.remoteParamValue = remoteParamValue;
            this.hello = helloImpl;
        }

        public void run() {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                jobThread();
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                System.out.println("Each loop done");
            }


        }

        public void jobThread() {
            while (true) {
                try {
                    String remoteReturnValue = hello.showTime(String.format("hhhh-mm-ss:%s", remoteParamValue));
                    System.out.println(remoteReturnValue);
                } catch (DoesNotConnectException e) {
                    System.out.println("Does not connect");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ignored) { }
                } catch (HelloImpl.IDoNotLikeException e) {
                    e.printStackTrace();
                }

            }
        }
    };








}
