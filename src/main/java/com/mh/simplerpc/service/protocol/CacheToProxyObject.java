package com.mh.simplerpc.service.protocol;

import com.mh.simplerpc.config.ConsumerEntity;
import com.mh.simplerpc.config.ProviderEntity;
import com.mh.simplerpc.exceptions.UnknownResourceException;
import com.mh.simplerpc.service.protocol.invocation.InvocationHandlerImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;

public class CacheToProxyObject {

    private HashMap<String,ConsumerEntity> consumerEntityMap = new HashMap<String, ConsumerEntity>();
    private HashMap<String,Object> proxyObjectMap = new HashMap<String, Object>();

    private InvocationHandler invocationHandler;
    private CallRemote callRemote;


    public CacheToProxyObject(List<ConsumerEntity> consumerEntityList,InvocationHandler invocationHandler) {
        for (ConsumerEntity item:consumerEntityList) {
            consumerEntityMap.put(item.getRes(),item);
        }

        this.invocationHandler = invocationHandler;
    }

    public CacheToProxyObject(List<ConsumerEntity> consumerEntityList,CallRemote callRemote) {
        for (ConsumerEntity item:consumerEntityList) {
            consumerEntityMap.put(item.getRes(),item);
        }

        this.callRemote = callRemote;
    }

    @SuppressWarnings("WeakerAccess")
    public Object getProxyObject(String res) throws UnknownResourceException,IllegalArgumentException {

        Object proxyObject = proxyObjectMap.get(res);
        if (proxyObject != null) return proxyObject;

        ConsumerEntity consumerEntity = consumerEntityMap.get(res);
        if (consumerEntity == null) {
            throw new UnknownResourceException(res);
        }

//        proxyObject = Proxy.newProxyInstance(
//                this.getClass().getClassLoader(),
//                new Class[]{consumerEntity.getToInterface()},
//                invocationHandler
//        );

        proxyObject = Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{consumerEntity.getToInterface()},
                new InvocationHandlerImpl(callRemote,res)
        );

        proxyObjectMap.put(res,proxyObject);

        return proxyObject;
    }

    public Object getProxyObject(Class<?> classes) throws UnknownResourceException,IllegalArgumentException {
        return  getProxyObject(classes.getName());
    }


}
