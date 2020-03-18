/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service.protocol;

import com.mh.simplerpc.SimpleRPCServiceApplication;

import java.util.HashMap;

public class CacheToClasses {

    // Will be load to JVM stack memory

    /*
    * map model class name to class
    *
    * */
    private static HashMap<String,Class<?>> classesMap = new HashMap<String, Class<?>>();

    public static Class<?> stringToClass(String className) throws ClassNotFoundException {

        Class<?> matchClass = classesMap.get(className);
        if (matchClass != null) return matchClass;
        Class<?> forNameClass;
        synchronized (CacheToClasses.class) {
            if (SimpleRPCServiceApplication.isStartCommand()) {
                forNameClass = SimpleRPCServiceApplication.getCommandClassLoader().loadClass(className);
            } else {
                forNameClass = Class.forName(className);
            }
            classesMap.put(className,forNameClass);
        }

        return forNameClass;
    }


}
