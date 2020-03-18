/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service.protocol;

import com.mh.simplerpc.config.ProviderEntity;
import com.mh.simplerpc.exceptions.NotEmptyConstructorException;
import com.mh.simplerpc.exceptions.UnknownResourceException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/*
* design allow multiple object
*
*
*
* */
public class CacheToInvokeObject {

    // load model
    private HashMap<String,ProviderEntity> providerEntityMap = new HashMap<String, ProviderEntity>();

    private HashMap<String,Object> instanceObjectMap = new HashMap<String, Object>();
    private HashMap<String,Method> invokeMethodMap = new HashMap<String, Method>();


    public CacheToInvokeObject(List<ProviderEntity> providerEntityList) {

        for (ProviderEntity item:providerEntityList) {
            providerEntityMap.put(item.getRes(),item);
        }

    }

    public Object getInstantiationObject(String res) throws UnknownResourceException,NotEmptyConstructorException,IllegalAccessException, InstantiationException {
        Object instanceObject = instanceObjectMap.get(res);
        if (instanceObject != null) return instanceObject;
        synchronized (CacheToInvokeObject.this) {
            // again check object is load
            instanceObject = instanceObjectMap.get(res);
            if (instanceObject != null) return instanceObject;

            ProviderEntity providerEntity = providerEntityMap.get(res);// unknown object
            if (providerEntity == null) throw new UnknownResourceException(res);

            Class<?> instanceObjClass = providerEntity.getToEntityClass();
            for (Constructor item:instanceObjClass.getConstructors()) {
                if (item.getParameterTypes().length <= 0) {
                    instanceObject = instanceObjClass.newInstance();
                    instanceObjectMap.put(res,instanceObject);
                    return instanceObject;
                }
            }
            throw new NotEmptyConstructorException();
        }
    }


    /*
    *
    * match model
    *
    * res.methodName.methodParamNumberGroup
    *
    * '/11/11.methodName.["","",""]'
    * 'packageName.className.methodName.["age1","age2","age3"]'
    *
    * be cale
    *
    * */
    public Method getInvokeObjectMethod(String res,String methodName,String[] methodParamType)
            throws UnknownResourceException, NoSuchMethodException, ClassNotFoundException {
        String key = String.format("%s.%s.%s",res,methodName, Arrays.toString(methodParamType));

        Method method = invokeMethodMap.get(key);
        if (method != null) return method;
        synchronized (CacheToInvokeObject.this) {
            method = invokeMethodMap.get(key);
            if (method != null) return method;

            ProviderEntity providerEntity = providerEntityMap.get(res);// unknown object
            if (providerEntity == null) {
                throw new UnknownResourceException(res);
            }

            Class<?>[] methodParamGroup = new Class[methodParamType.length];
            for (int i = 0;i < methodParamType.length;i++) {
                methodParamGroup[i] = CacheToClasses.stringToClass(methodParamType[i]);
            }
            method = providerEntity.getToEntityClass().getMethod(methodName,methodParamGroup);
            invokeMethodMap.put(key,method);
        }

        return method;
    }




}
