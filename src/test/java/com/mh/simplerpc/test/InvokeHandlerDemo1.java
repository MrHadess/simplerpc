package com.mh.simplerpc.test;

import com.google.gson.JsonElement;
import com.mh.simplerpc.ServiceManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class InvokeHandlerDemo1 implements InvocationHandler {

    private InvocationHandler waitObj;

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        test2.InvokeNeed invokeNeed = new test2.InvokeNeed();
        invokeNeed.res = method.getDeclaringClass().getName();
        invokeNeed.methodName = method.getName();

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length <= 0) {
            invokeNeed.methodParam = new String[0];
            invokeNeed.args = new String[0];
        } else {
            String[] serParameter = new String[parameterTypes.length];
            String[] serArgs = new String[parameterTypes.length];
            for (int i = 0;i < parameterTypes.length;i++) {
                serParameter[i] = parameterTypes[i].getName();
                serArgs[i] = ServiceManager.getGson().toJson(args[i],parameterTypes[i]);
            }
            invokeNeed.methodParam = serParameter;
            invokeNeed.args = serArgs;
        }

        System.out.println("invokeCode");
        System.out.println(ServiceManager.getGson().toJson(invokeNeed));
        String acceptData = ServiceManager.getGson().toJson(invokeNeed);
//        JsonElement jsonElement = remoteRun(acceptData);

        final ResulttImpl resultt1 = new ResulttImpl();

        final Object waitObj2 = this;
//        final Thread nowThread = Thread.currentThread();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                resultt1.returnType("work....");
                synchronized (waitObj2){
                    waitObj2.notify();
                }


            }
        }).start();


        synchronized (waitObj2) {
            waitObj2.wait();
        }



        return resultt1.object;
    }

    class ResulttImpl implements Resultt {

        private Object object;
        public void returnType(Object o) {
            object = o;
        }
    }

    interface Resultt{
        void returnType(Object o);
    }



}
