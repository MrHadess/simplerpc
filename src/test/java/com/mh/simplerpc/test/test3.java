package com.mh.simplerpc.test;

import com.mh.simplerpc.ServiceConfig;
import com.mh.simplerpc.ServiceManager;
import com.mh.simplerpc.config.ConsumerEntity;
import com.mh.simplerpc.config.ProviderEntity;
import org.junit.Test;

import java.util.Arrays;


public class test3 {

    @Test
    public void test1A() {
        ProviderEntity providerEntity = new ProviderEntity(Hello.class,HelloImpl.class);
        ServiceConfig serviceConfig = new ServiceConfig.Builder()
                .setRemoteIP("127.0.0.1")
                .setJobMode(1)
                .setProviderConfigList(Arrays.asList(providerEntity))
                .build();

        ServiceManager serviceManager = new ServiceManager(serviceConfig);
        serviceManager.startup();

    }

    @Test
    public void test1B() {
        ConsumerEntity consumerEntity = new ConsumerEntity(Hello.class);

        ServiceConfig serviceConfig = new ServiceConfig.Builder()
                .setRemoteIP("127.0.0.1")
                .setJobMode(2)
                .setConsumerConfigList(Arrays.asList(consumerEntity))
                .build();

        final ServiceManager serviceManager = new ServiceManager(serviceConfig);
        System.out.println("ready start");



        System.out.println("run new thread");
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(3000);

                    Hello hello = serviceManager.getBean(Hello.class);

                    for (int i=0;i<100;i++) {
                        System.out.println(
                                String.format("Im get time:%s",
                                        hello.showTime(String.format("yyyy-MM-dd-%s", i))
                                )
                        );
//                        Thread.sleep(1000);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }



            }
        });

        thread.start();



        serviceManager.startup();

    }

    private void checkThread(final Thread thread) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.State state = null;
                    while (thread.isAlive()) {
//                        Thread.sleep(100);
//                        boolean tempstate = thread.isInterrupted();
                        Thread.State tempstate = thread.getState();
                        if (state == null || tempstate != state){
                            state = tempstate;
                            System.out.println(String.format("Thread Interrupted state:%s",state));
                        }

//                        System.out.println(String.format("Thread Interrupted state:%s",thread.isInterrupted()));
                    }
                    System.out.println(String.format("Thread Alive state:%s",thread.isAlive()));

//                } catch (InterruptedException e) {
//                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }











}
