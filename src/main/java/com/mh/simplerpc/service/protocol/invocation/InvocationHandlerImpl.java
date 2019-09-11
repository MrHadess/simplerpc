package com.mh.simplerpc.service.protocol.invocation;

import com.google.gson.Gson;
import com.mh.simplerpc.ServiceManager;
import com.mh.simplerpc.pojo.InvokeObjectInfo;
import com.mh.simplerpc.service.protocol.CallRemote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class InvocationHandlerImpl implements InvocationHandler {

    private static Logger logger = LoggerFactory.getLogger(InvocationHandlerImpl.class);

    private CallRemote callRemote;
    private String res;
    private static Gson gson = ServiceManager.getGson();

    public InvocationHandlerImpl(CallRemote callRemote) {
        this.callRemote = callRemote;
    }

    public InvocationHandlerImpl(CallRemote callRemote,String res) {
        this.callRemote = callRemote;
        this.res = res;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        InvokeObjectInfo invokeObjectInfo = new InvokeObjectInfo();
//        invokeObjectInfo.setRes(method.getDeclaringClass().getName());
        invokeObjectInfo.setRes(res);
        invokeObjectInfo.setMethodName(method.getName());


        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length <= 0) {
            invokeObjectInfo.setMethodParam(new String[0]);
            invokeObjectInfo.setArgs(new String[0]);
        } else {
            String[] serParameter = new String[parameterTypes.length];
            String[] serArgs = new String[parameterTypes.length];
            for (int i = 0;i < parameterTypes.length;i++) {
                serParameter[i] = parameterTypes[i].getName();
                logger.debug(String.format("parameterTypes[%s]",i),parameterTypes[i].getName());
                serArgs[i] = gson.toJson(args[i],parameterTypes[i]);
            }
            invokeObjectInfo.setMethodParam(serParameter);
            invokeObjectInfo.setArgs(serArgs);
        }


        InvokeResultImpl invokeResult = new InvokeResultImpl(method.getGenericReturnType());
        // do something
        callRemote.call(invokeObjectInfo,invokeResult,Thread.currentThread());

        synchronized (invokeResult) {
            final InvokeResultImpl newInvokeResultImpl = (InvokeResultImpl) invokeResult.clone();
            callRemote.recoveryCallObject(newInvokeResultImpl.getProcessID());
//            System.out.println(String.format("print newInvokeResultImpl :%s",newInvokeResultImpl));
//        Thread.sleep(2000);
            if (newInvokeResultImpl.isHasException()) {
                throw newInvokeResultImpl.getException();
            }

            if (newInvokeResultImpl.isHasReturn()) {
                return newInvokeResultImpl.getReturnObject();
            }
        }

//        System.out.println("-----------------Thread notify");

//        thread notify run next return value or throw exception


//        final InvokeResultImpl newInvokeResultImpl = (InvokeResultImpl) invokeResult.clone();
//        callRemote.recoveryCallObject(newInvokeResultImpl.getProcessID());
//        System.out.println(String.format("print newInvokeResultImpl :%s",newInvokeResultImpl));
////        Thread.sleep(2000);
//        if (newInvokeResultImpl.isHasException()) {
//            throw newInvokeResultImpl.getException();
//        }
//
//        if (newInvokeResultImpl.isHasReturn()) {
//            return newInvokeResultImpl.getReturnObject();
//        }


//        System.out.println(invokeResult);
//        if (invokeResult.isHasException()) {
//            throw invokeResult.getException();
//        }
//
//        if (invokeResult.isHasReturn()) {
//            return invokeResult.getReturnObject();
//        }
        return null;
    }


}
