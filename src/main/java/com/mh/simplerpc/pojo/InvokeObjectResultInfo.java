package com.mh.simplerpc.pojo;

import com.google.gson.Gson;
import com.mh.simplerpc.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class InvokeObjectResultInfo {

    private static Logger logger = LoggerFactory.getLogger(InvokeObjectResultInfo.class);
    private static Gson json = ServiceManager.getGson();

    private state invokeState;

    private String exceptionStack;
    private String exceptionType;

    private String returnType;
    private String arg;

    private static final String VOID = "void";

    public InvokeObjectResultInfo() { }

    InvokeObjectResultInfo(Builder builder) {
        this.arg = builder.arg;
        this.returnType = builder.returnType;
        this.invokeState = builder.invokeState;
        this.exceptionStack = builder.toStrExceptionStack;
        this.exceptionType = builder.toStrExceptionType;
    }


    public String getReturnType() {
        return returnType;
    }

    public String getArg() {
        return arg;
    }

    public state getInvokeState() {
        return invokeState;
    }

    public String getExceptionStack() {
        return exceptionStack;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    @Override
    public String toString() {
        return "InvokeObjectResultInfo{" +
                "invokeState=" + invokeState +
                ", exceptionStack='" + exceptionStack + '\'' +
                ", exceptionType='" + exceptionType + '\'' +
                ", returnType='" + returnType + '\'' +
                ", arg='" + arg + '\'' +
                '}';
    }

    public String toSimpleInfo() {
        return "InvokeObjectResultInfo{" +
                "invokeState=" + invokeState +
                ", returnType='" + returnType + '\'' +
                '}';
    }

    public enum state {
        UnknownResult,
        HasResult,
        HasException

    }

    public static class Builder {

        private state invokeState;

        private String toStrExceptionStack;
        private String toStrExceptionType;

        private String returnType;
        private String arg;

        public Builder setSuccessInfo(Class<?> returnTypeClass, Object arg, SerializableObject serializableObject) {
            String returnType;
            if (VOID.equals((returnType = returnTypeClass.getName()))) {
                invokeState = state.UnknownResult;
                this.returnType = returnType;
            } else {
                invokeState = state.HasResult;
                this.returnType = returnType;
                this.arg = serializableObject.serializableObj(arg);
            }
            return this;
        }

        public Builder setException(Throwable exceptionCause) {
            String throwableClassName = exceptionCause.getClass().getName();
            toStrExceptionStack = json.toJson(exceptionCause);
            toStrExceptionType = throwableClassName;
            invokeState = state.HasException;
            return this;
        }

        public InvokeObjectResultInfo build() {
            return new InvokeObjectResultInfo(this);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "invokeState=" + invokeState +
                    ", toStrExceptionStack='" + toStrExceptionStack + '\'' +
                    ", toStrExceptionType='" + toStrExceptionType + '\'' +
                    ", returnType='" + returnType + '\'' +
                    ", arg='" + arg + '\'' +
                    '}';
        }
    }

    public interface SerializableObject {
        String serializableObj(Object o);
    }


}
