/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service.protocol.invocation;

import com.mh.simplerpc.pojo.InvokeObjectInfo;
import com.mh.simplerpc.service.protocol.CallRemote;

import java.lang.reflect.Type;

public class InvokeResultImpl implements InvokeResult,InvokeState,Cloneable {

    private Type type;

    private boolean hasException = false;
    private Throwable exception;

    private boolean hasReturn;
    private Object returnObject;

    private String processID;
    private int handlerState = 0;// '0'-UnknownUnlockOrLock '-1'-RunUnlock '1'-RunLock

    public InvokeResultImpl(Type type) {
        this.type = type;
    }

    public void call(CallRemote callRemote,InvokeObjectInfo invokeObjectInfo) {
        synchronized (this) {
            callRemote.call(invokeObjectInfo, this, this);
        }
    }

    @Override
    public void requestSuccess() {
        if (handlerState >= 0) {
            handlerState++;
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void responseData() {
        synchronized (this) {
            if (handlerState == 0) {
                handlerState--;
                return;
            }
            if (handlerState >= 1) {
                this.notify();
            }
        }
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
