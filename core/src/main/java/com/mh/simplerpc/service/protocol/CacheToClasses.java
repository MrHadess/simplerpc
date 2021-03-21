/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service.protocol;

import com.mh.simplerpc.SimpleRPCServiceApplication;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheToClasses {

    // Will be load to JVM stack memory
    private static final ConcurrentHashMap.KeySetView<String,Boolean> processMap = ConcurrentHashMap.newKeySet();

    private static final Map<String,AtomicInteger> processMapp = new ConcurrentHashMap<>();
    // state

    /*
    * map model class name to class
    *
    * */
    private static final Map<String,Class<?>> classesMap = new ConcurrentHashMap<>();

    public static Class<?> stringToClass(String className) throws ClassNotFoundException {

        Class<?> matchClass = classesMap.get(className);
        if (matchClass != null) return matchClass;

        AtomicInteger num = processMapp.putIfAbsent(className,new AtomicInteger(0));
        if (num == null) {
            System.out.println(String.format("Thread name %s",Thread.currentThread().getName()));
            Class<?> forNameClass;
            try {
                if (SimpleRPCServiceApplication.isStartCommand()) {
                    forNameClass = SimpleRPCServiceApplication.getCommandClassLoader().loadClass(className);
                } else {
                    forNameClass = Class.forName(className);
                }
//                try {
//                    TimeUnit.SECONDS.sleep(1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            } catch (ClassNotFoundException e) {
                int nowValue = processMapp.get(className).decrementAndGet();
                if (nowValue < 0) {
                    processMap.remove(className);
                }
                throw e;
            }

            // check again key
            if (!classesMap.containsKey(className)) {
                classesMap.put(className,forNameClass);
            }

            processMapp.get(className).decrementAndGet();
            return forNameClass;
        }


//        int processOrderNum = num.incrementAndGet();
        int processOrderNum = num.getAndIncrement();
        AtomicInteger tempNumAtomic;
        while ((tempNumAtomic = processMapp.get(className)) != null && tempNumAtomic.get() > processOrderNum) {
            System.out.println(String.format("Thread %s wait",Thread.currentThread().getName()));
        }

        // unlock next
        int nowValue = num.decrementAndGet();
        if (nowValue < 0) {
            processMap.remove(className);
        }
        matchClass = classesMap.get(className);
        if (matchClass == null) {
            throw new ClassNotFoundException(className);
        }
        return matchClass;

//        if (processMap.contains(className)) {
//            Class<?> forNameClass;
//            try {
//                if (SimpleRPCServiceApplication.isStartCommand()) {
//                    forNameClass = SimpleRPCServiceApplication.getCommandClassLoader().loadClass(className);
//                } else {
//                    forNameClass = Class.forName(className);
//                }
//            } catch (ClassNotFoundException e) {
//                processMap.remove(className);
//                throw e;
//            }
//
//            // check again key
//            if (!classesMap.containsKey(className)) {
//                classesMap.put(className,forNameClass);
//            }
//        }

//        Class<?> forNameClass;
//        if (SimpleRPCServiceApplication.isStartCommand()) {
//            forNameClass = SimpleRPCServiceApplication.getCommandClassLoader().loadClass(className);
//        } else {
//            forNameClass = Class.forName(className);
//        }
//
//        // check again key
//        if (!classesMap.containsKey(className)) {
//            classesMap.put(className,forNameClass);
//        }

//        return forNameClass;
//        return classesMap.get(className);
    }


}
