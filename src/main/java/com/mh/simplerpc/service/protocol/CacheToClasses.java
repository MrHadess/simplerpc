package com.mh.simplerpc.service.protocol;

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
            forNameClass = Class.forName(className);
            classesMap.put(className,forNameClass);
        }

        return forNameClass;
    }


}
