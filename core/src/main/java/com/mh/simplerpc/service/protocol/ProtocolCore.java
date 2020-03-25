/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service.protocol;

import com.google.gson.Gson;
import com.mh.simplerpc.ServiceConfig;
import com.mh.simplerpc.ServiceManager;
import com.mh.simplerpc.SimpleRPCServiceApplication;
import com.mh.simplerpc.dto.ProviderInfo;
import com.mh.simplerpc.exceptions.*;
import com.mh.simplerpc.pojo.InvokeObjectInfo;
import com.mh.simplerpc.pojo.InvokeObjectResultInfo;
import com.mh.simplerpc.service.ServiceProtocol;
import com.mh.simplerpc.service.protocol.invocation.InvocationHandlerImpl;
import com.mh.simplerpc.service.protocol.invocation.InvokeResult;
import com.mh.simplerpc.service.protocol.invocation.InvokeState;
import com.mh.simplerpc.service.protocol.result.CallToFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProtocolCore implements ServiceProtocol.Invocation,CallRemote {

    private static Logger logger = LoggerFactory.getLogger(ProtocolCore.class);
    private static Logger invokeObjectRunnableLogger = LoggerFactory.getLogger(InvokeObjectRunnable.class);

    private ServiceProtocol.Result resultControl;
    private Gson gson = ServiceManager.getGson();

    private boolean connectState = false;

    private HashSet<String> remoteSupportResSet = new HashSet<String>();
    private HashMap<String,InvokeResult> invokeResultMap = new HashMap<String, InvokeResult>();
    private HashMap<String,InvokeState> invokeStateMap = new HashMap<String, InvokeState>();

    private InvocationHandler invocationHandler = new InvocationHandlerImpl(this);
    private CallToFunction callToFunction;

    private CacheToInvokeObject cacheToInvokeObject;
    private CacheToProxyObject cacheToProxyObject;


    // Job thread
    private ExecutorService cacheExecutorService = Executors.newCachedThreadPool();



    public ProtocolCore(ServiceConfig serviceConfig) {
        cacheToInvokeObject = new CacheToInvokeObject(serviceConfig.getProviderConfigList());
//        cacheToProxyObject = new CacheToProxyObject(serviceConfig.getConsumerConfigList(),invocationHandler);
        cacheToProxyObject = new CacheToProxyObject(serviceConfig.getConsumerConfigList(),this);

        callToFunction = new CallToFunction(cacheToInvokeObject);


    }

    public void updateIsConnectState(boolean state) {
        if (connectState && !state) { // In connect state,and now disconnect network
            // remove before destroy job
            InvokeObjectResultInfo invokeObjectInfo = new InvokeObjectResultInfo.Builder()
                    .setException(new ConnectionDisconnectException())
                    .build();
            for (String processID:invokeResultMap.keySet()) {
                cacheExecutorService.execute(new AcceptInvokeObjectResultRunnable(processID,invokeObjectInfo));
            }
        }
        this.connectState = state;
        remoteSupportResSet = new HashSet<String>();
    }

    public void acceptProviderServices(List<ProviderInfo> providerInfoList) {
        HashSet<String> tempRemoteSupportResSet = new HashSet<String>();
        for (ProviderInfo item:providerInfoList) {
            tempRemoteSupportResSet.add(item.getRes());
        }
        this.remoteSupportResSet = tempRemoteSupportResSet;
    }

    public void acceptInvokeObject(String processID, InvokeObjectInfo invokeObjectInfo) {
        cacheExecutorService.execute(new InvokeObjectRunnable(processID,invokeObjectInfo));
    }

    class InvokeObjectRunnable implements Runnable {

        private String processID;
        private InvokeObjectInfo invokeObjectInfo;
        private InvokeObjectResultInfo invokeObjectResultInfo;

        InvokeObjectRunnable(String processID, InvokeObjectInfo invokeObjectInfo) {
            this.processID = processID;
            this.invokeObjectInfo = invokeObjectInfo;
            if (SimpleRPCServiceApplication.isStartCommand()) {
                Thread.currentThread().setContextClassLoader(SimpleRPCServiceApplication.getCommandClassLoader());
            }
        }

        public void run() {
            try {
                invokeObjectResultInfo = callToFunction.call(invokeObjectInfo);
                resultControl.remoteInvokeObjectResult(processID, invokeObjectResultInfo);
                return;
            } catch (NotEmptyConstructorException e) {
                invokeObjectRunnableLogger.error("NotEmptyConstructorException",e);
                invokeObjectResultInfo = new InvokeObjectResultInfo.Builder().setException(e).build();
            } catch (InstantiationException e) {
                invokeObjectRunnableLogger.error("InstantiationException",e);
                invokeObjectResultInfo = new InvokeObjectResultInfo.Builder().setException(e).build();
            } catch (IllegalAccessException e) {
                invokeObjectRunnableLogger.error("IllegalAccessException",e);
                invokeObjectResultInfo = new InvokeObjectResultInfo.Builder().setException(e).build();
            } catch (NoSuchMethodException e) {
                invokeObjectRunnableLogger.error("NoSuchMethodException",e);
                invokeObjectResultInfo = new InvokeObjectResultInfo.Builder().setException(e).build();
            } catch (ClassNotFoundException e) {
                invokeObjectRunnableLogger.error("ClassNotFoundException",e);
                invokeObjectResultInfo = new InvokeObjectResultInfo.Builder().setException(e).build();
            } catch (InvocationTargetException e) {
                invokeObjectRunnableLogger.error("InvocationTargetException",e);
                invokeObjectResultInfo = new InvokeObjectResultInfo.Builder().setException(e).build();
            }

            if (!connectState) {
                throw new DoesNotConnectException();
            }

            // default exception will return null
            resultControl.remoteInvokeObjectResult(processID,invokeObjectResultInfo);
        }
    }

    public void acceptInvokeObjectResult(String processID, InvokeObjectResultInfo invokeObjectResultInfo) {
        InvokeResult invokeResult = invokeResultMap.get(processID);
        switch (invokeObjectResultInfo.getInvokeState()) {
            case UnknownResult:
                invokeResult.returnType(false, null);
                break;
            case HasResult:
                Object obj = null;
                try {
                    logger.debug(CacheToClasses.stringToClass(invokeObjectResultInfo.getReturnType()).getName());
//                obj = gson.fromJson(invokeObjectResultInfo.getArg(),CacheToClasses.stringToClass(invokeObjectResultInfo.getReturnType()));
                    obj = gson.fromJson(invokeObjectResultInfo.getArg(),invokeResult.getType());
                } catch (ClassNotFoundException e) {
                    logger.error("Find empty return type",e);
                }
                invokeResult.returnType(true, obj);
                break;
            case HasException:
                try {
                    Throwable throwable;
                    Class<?> exceptionType = CacheToClasses.stringToClass(invokeObjectResultInfo.getExceptionType());
                    if (!Throwable.class.isAssignableFrom(exceptionType)) {
                        logger.error("Exception unable extends throwable",invokeObjectResultInfo);
                        invokeResult.runException(new RemoteInvokeException(invokeObjectResultInfo.getExceptionStack()));
                        break;
                    }
                    throwable = Throwable.class.cast(gson.fromJson(invokeObjectResultInfo.getExceptionStack(),exceptionType));
                    invokeResult.runException(throwable);
                } catch (ClassNotFoundException e) {
                    logger.info("Find empty exception type",e);
                    Throwable throwable = gson.fromJson(invokeObjectResultInfo.getExceptionStack(),Throwable.class);
                    invokeResult.runException(new RemoteInvokeException(throwable));
                }
                break;
        }

        InvokeState invokeState = invokeStateMap.get(processID);
        invokeState.responseData();

        invokeResultMap.remove(processID);
        invokeStateMap.remove(processID);

    }

    class AcceptInvokeObjectResultRunnable implements Runnable {

        private String processID;
        private InvokeObjectResultInfo invokeObjectResultInfo;

        public AcceptInvokeObjectResultRunnable(String processID, InvokeObjectResultInfo invokeObjectResultInfo) {
            this.processID = processID;
            this.invokeObjectResultInfo = invokeObjectResultInfo;
        }

        @Override
        public void run() {
            acceptInvokeObjectResult(processID,invokeObjectResultInfo);
        }
    }

    @Override
    public void call(InvokeObjectInfo invokeObjectInfo, InvokeResult invokeResult, InvokeState invokeState) {
        //  allocation processID
        if (!connectState) throw new DoesNotConnectException();
        if (!remoteSupportResSet.contains(invokeObjectInfo.getRes())) throw new RemoteUnsupportedResException();

        String processID = UUID.randomUUID().toString();
        invokeResultMap.put(processID,invokeResult);
        invokeStateMap.put(processID,invokeState);
        try {
            resultControl.remoteInvokeObject(processID,invokeObjectInfo);
        } catch (DoesNotConnectException e) {
            // If request fail recovery data
            invokeResultMap.remove(processID);
            invokeStateMap.remove(processID);
            throw e;
        }

        invokeState.requestSuccess();
    }

    public void setResultControl(ServiceProtocol.Result resultControl) {
        this.resultControl = resultControl;
    }


    public <T> T getBean(Class<T> classes) throws UnknownResourceException,IllegalArgumentException {
        Object object = cacheToProxyObject.getProxyObject(classes);
        return classes.cast(object);
    }

    public Object getBean(String res) throws UnknownResourceException,IllegalArgumentException {
        return cacheToProxyObject.getProxyObject(res);
    }


}
