package com.mh.simplerpc.service.protocol.invocation;

import java.lang.reflect.Type;

public class InvokeResultImpl implements InvokeResult,Cloneable {

    private Type type;

    private boolean hasException = false;
    private Throwable exception;

    private boolean hasReturn;
    private Object returnObject;

    private String processID;

    public InvokeResultImpl(Type type) {
        this.type = type;
    }


    public void returnType(boolean hasReturn, Object o) {
        this.hasReturn = hasReturn;
        this.returnObject = o;
    }

    public void runException(Throwable e) {
        this.exception = e;
        this.hasException = true;
    }

    public String getProcessID() {
        return processID;
    }

    public Type getType() {
        return type;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public boolean isHasReturn() {
        return hasReturn;
    }

    public Object getReturnObject() {
        return returnObject;
    }

    public boolean isHasException() {
        return hasException;
    }

    public Throwable getException() {
        return exception;
    }

    @Override
    public String toString() {
        return "InvokeResultImpl{" +
                "hasException=" + hasException +
                ", exception=" + exception +
                ", hasReturn=" + hasReturn +
                ", returnObject=" + returnObject +
                '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
//        return super.clone();
        InvokeResultImpl invokeResult = new InvokeResultImpl(type);
        invokeResult.exception = this.exception;
        invokeResult.hasException = this.hasException;
        invokeResult.hasReturn = this.hasReturn;
        invokeResult.returnObject = this.returnObject;
        return invokeResult;
    }
}
