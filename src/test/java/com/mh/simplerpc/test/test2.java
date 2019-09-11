package com.mh.simplerpc.test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.mh.simplerpc.ServiceConfig;
import com.mh.simplerpc.ServiceManager;
import com.mh.simplerpc.config.ConsumerEntity;
import com.mh.simplerpc.config.ProviderEntity;
import com.mh.simplerpc.service.ConfigCheck;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class test2 {

    @Test
    public void testB() {

    }

    class XX implements Serializable{}


    @Test
    public void testA() throws IllegalAccessException, InstantiationException {


        remoteCreate2();

        Class<?> unknownClass = Hello.class;

//        Object object = unknownClass.newInstance();
//        unknownClass.getMethod()

//        Object oo = Proxy.getProxyClass(null,unknownClass);
//        Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{unknownClass}, new InvocationHandler() {
//            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//
//                System.out.println(method.getName());
//                if (method.getReturnType() == null) {
//                    System.out.println("unknown return type");
//                } else {
//                    System.out.println("has return type");
//                }
//
//
//
//                return null;
//            }
//        });

//        Hello hello = (Hello) getBean2(unknownClass);
//        Hello hello = getBean2(Hello.class);
        final Hello hello = getBean2(Hello.class);
//        final Hello hello = getBean3(Hello.class);

//        hello.showTime();
        System.out.println(hello.showTime());

//        for(int i=0;i<10;i++){
//            final int finalI = i;
//            new Thread(new Runnable() {
//                public void run() {
//                    System.out.println(hello.showTime(String.format("yyyy-MM-dd-%s", finalI)));
//                }
//            }).start();
//
//        }
//
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


//        System.out.println(hello.showTime("yyyy-MM-dd"));

//        hello.remoteShowTime();

//        Date date = new Date();
//        System.out.println(String.format("native time:%s",date));
//        hello.testSerializableObjectTime(date);



    }

    private HashMap<String,Object> objMap = new HashMap<String, Object>();
    private HashMap<String,Class<?>> classMap = new HashMap<String, Class<?>>();
    private void remoteCreate(ServiceConfig serviceConfig) throws IllegalAccessException, InstantiationException {
        List<ProviderEntity> providerEntityList = serviceConfig.getProviderConfigList();
        for (ProviderEntity item:providerEntityList) {
            Object entityObject = item.getToEntityClass().newInstance();
            objMap.put(item.getRes(),entityObject);
        }
    }

    private void remoteCreate2() throws IllegalAccessException, InstantiationException {
        Object entityObject = HelloImpl.class.newInstance();
        objMap.put(Hello.class.getName(), entityObject);
        classMap.put(Hello.class.getName(),HelloImpl.class);
    }

    public <T> T getBean(Class<T> tClass){
        Object object = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{tClass}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                System.out.println(method.getDeclaringClass());
                System.out.println(method.getName());
                System.out.println("-------------------------");
                for (Class<?> item:method.getParameterTypes()) {
                    System.out.println(item.toString());
                }
                System.out.println("-------------------------");

                if (method.getReturnType() == null) {
                    System.out.println("unknown return type");
                } else {
                    System.out.println("has return type");
                }


                return new Date().toString();
            }
        });
        return tClass.cast(object);
    }

    private  <T> T getBean2(Class<T> tClass) {
        Object object = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{tClass}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                InvokeNeed invokeNeed = new InvokeNeed();
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
                JsonElement jsonElement = remoteRun(acceptData);


                Thread.sleep(5000);


                if (method.getReturnType().getName().equals("void")) {
                    System.out.println("unknown return type");
                    return null;
                } else {
                    System.out.println("has return type");
                    return ServiceManager.getGson().fromJson(jsonElement,method.getReturnType());
                }

//                return new Date().toString();
            }
        });
        return tClass.cast(object);
    }

    private  <T> T getBean3(Class<T> tClass) {
        Object object = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{tClass},new InvokeHandlerDemo1());
        return tClass.cast(object);
    }

    private JsonElement remoteRun(String acceptData) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Gson gson = ServiceManager.getGson();
        InvokeNeed invokeNeed = gson.fromJson(acceptData,InvokeNeed.class);

        Class<?>[] paramClasss = null;
        Object[] paramObjects = null;
        if (invokeNeed.methodParam.length <= 0) {
            paramClasss = new Class[]{};
            paramObjects = new Object[]{};
        } else {
            paramClasss = new Class[invokeNeed.methodParam.length];
            paramObjects = new Object[invokeNeed.methodParam.length];
            for (int i = 0;i < invokeNeed.methodParam.length;i++) {
                paramClasss[i] = Class.forName(invokeNeed.methodParam[i]);
                paramObjects[i] = gson.fromJson(invokeNeed.args[i],paramClasss[i]);
            }
        }

//        Method method = classMap.get(invokeNeed.res).getDeclaredMethod(invokeNeed.methodName,paramClasss);
        Method method = classMap.get(invokeNeed.res).getMethod(invokeNeed.methodName,paramClasss);// will be lost speed
        Object resultObject = method.invoke(objMap.get(invokeNeed.res),paramObjects);

        if ("void".equals(method.getReturnType().getName())) return null;
        return ServiceManager.getGson().toJsonTree(resultObject,method.getReturnType());
    }

    static class InvokeNeed {

        public String res;
        public String methodName;
        public String[] methodParam;
        public String[] args;

        @Override
        public String toString() {
            return "InvokeNeed{" +
                    "res='" + res + '\'' +
                    ", methodName='" + methodName + '\'' +
                    ", methodParam=" + Arrays.toString(methodParam) +
                    ", args=" + Arrays.toString(args) +
                    '}';
        }

    }




}
