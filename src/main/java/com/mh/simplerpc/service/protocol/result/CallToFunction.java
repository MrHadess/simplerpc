/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service.protocol.result;

import com.google.gson.Gson;
import com.mh.simplerpc.ServiceManager;
import com.mh.simplerpc.pojo.InvokeObjectInfo;
import com.mh.simplerpc.pojo.InvokeObjectResultInfo;
import com.mh.simplerpc.service.protocol.CacheToInvokeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class CallToFunction {

    private static Logger logger = LoggerFactory.getLogger(CallToFunction.class);

    private static final String VOID = "void";
    private CacheToInvokeObject cacheToInvokeObject;
    private Gson gson = ServiceManager.getGson();

    public CallToFunction(CacheToInvokeObject cacheToInvokeObject) {
        this.cacheToInvokeObject = cacheToInvokeObject;
    }

    public InvokeObjectResultInfo call(InvokeObjectInfo invokeObjectInfo)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, InvocationTargetException {

        Object providerObject = cacheToInvokeObject.getInstantiationObject(invokeObjectInfo.getRes());
        final Method invokeMethod = cacheToInvokeObject.getInvokeObjectMethod(
                invokeObjectInfo.getRes(),
                invokeObjectInfo.getMethodName(),
                invokeObjectInfo.getMethodParam()
        );

        String[] argsSerializableObj = invokeObjectInfo.getArgs();
        Type[] methodParamType = invokeMethod.getGenericParameterTypes();
        Object[] args = new Object[invokeObjectInfo.getMethodParam().length];
        for (int i = 0;i < methodParamType.length;i++) {
            // Deserialize need use method type to object
            args[i] = gson.fromJson(argsSerializableObj[i], methodParamType[i]);
        }

//        String[] methodParamName = invokeObjectInfo.getMethodParam();
//        for (int i = 0;i < methodParamName.length;i++) {
//            args[i] = gson.fromJson(argsSerializableObj[i], CacheToClasses.stringToClass(methodParamName[i]));
//        }

        Object invokeReturnValue;
        InvokeObjectResultInfo invokeObjectResultInfo;
//        try {
//            invokeReturnValue = invokeMethod.invoke(providerObject,args);
//        } catch (RuntimeException e) {
//            throw e;
//        }


        try {
            invokeReturnValue = invokeMethod.invoke(providerObject,args);
        } catch (InvocationTargetException e) {
            logger.error("Invoke object exception",e.getCause());

            invokeObjectResultInfo = new InvokeObjectResultInfo.Builder()
                    .setException(e.getCause())
//                    .setException(e.getTargetException())
                    .build();

            return  invokeObjectResultInfo;
        }

//        InvokeObjectResultInfo invokeObjectResultInfo = new InvokeObjectResultInfo(invokeMethod.getReturnType(),invokeMethod);
//        InvokeObjectResultInfo invokeObjectResultInfo = new InvokeObjectResultInfo.Builder()
        invokeObjectResultInfo = new InvokeObjectResultInfo.Builder()
                .setSuccessInfo(
                        invokeMethod.getReturnType(),
                        invokeReturnValue,
                        new InvokeObjectResultInfo.SerializableObject() {
                            public String serializableObj(Object o) {
//                                return gson.toJson(o,invokeMethod.getReturnType());
                                return gson.toJson(o,invokeMethod.getGenericReturnType());
                            }
                        }
                )
                .build();

        return invokeObjectResultInfo;
    }


}
