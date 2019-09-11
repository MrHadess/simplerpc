package com.mh.simplerpc.test;

import com.mh.simplerpc.ServiceConfig;
import com.mh.simplerpc.ServiceManager;
import com.mh.simplerpc.config.ConsumerEntity;
import com.mh.simplerpc.config.ProviderEntity;
import com.mh.simplerpc.exceptions.DoesNotConnectException;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;


public class test {

    @Test
    public void test1A() {
        ArrayList<ProviderEntity> list = new ArrayList<ProviderEntity>();

        ProviderEntity providerEntity = new ProviderEntity(Hello.class,HelloImpl.class);
//        ProviderEntity providerEntity = new ProviderEntity(Hello.class,test.class);
        list.add(providerEntity);
        ServiceConfig serviceConfig = new ServiceConfig.Builder()
                .setRemoteIP("127.0.0.1")
                .setJobMode(1)
//                .setProviderConfigList(Arrays.asList(providerEntity))
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
    }

    @Test
    public void test1B() {
        ConsumerEntity consumerEntity = new ConsumerEntity(Hello.class);

//        ServiceConfig serviceConfig = new ServiceConfig.Builder("127.0.0.1")
        ServiceConfig serviceConfig = new ServiceConfig.Builder()
                .setRemoteIP("183.234.44.89")
                .setJobMode(2)
                .setConsumerConfigList(Arrays.asList(consumerEntity))
                .build();

        final ServiceManager serviceManager = new ServiceManager(serviceConfig);
        serviceManager.startup();

        Hello h = serviceManager.getBean(Hello.class);
        new Thread(new Run1(1,h)).start();
//        new Thread(new Run1(2,h)).start();
//        new Thread(new Run1(3,h)).start();
//        new Thread(new Run1(4,h)).start();

        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    class Run1 implements Runnable {
        private int ii;
        private Hello h;

        public Run1(int ii, Hello h) {
            this.ii = ii;
            this.h = h;
        }

        public void run() {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            while (true) {
                try {
                    h.showTime(String.format("hhhh-mm-ss:%s",ii));
                } catch (DoesNotConnectException e) {
                    System.out.println("DoesNotConnect");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e1) {

                    }
                } catch (HelloImpl.IDontLikeException e) {
                    e.printStackTrace();
                }

            }
        }
    };












}
