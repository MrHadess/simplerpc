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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

public class InvokeJobHandlerImpl implements InvokeResult,InvokeState {

    private static Logger logger = LoggerFactory.getLogger(InvokeJobHandlerImpl.class);

    private static final int JOB_ACCEPT_DATA = -1;
    private static final int JOB_LOCK_AND_WAIT_RESPONSE = 1;

    private Type type;

    private boolean hasException = false;
    private Throwable exception;

    private boolean hasReturn;
    private Object returnObject;

    private int handlerState = 0;// '0'-UnknownUnlockOrLock '-1'-RunUnlock '1'-RunLock

    public InvokeJobHandlerImpl(Type type) {
        this.type = type;
    }

    public void call(CallRemote callRemote,InvokeObjectInfo invokeObjectInfo) {
        synchronized (this) {
            callRemote.call(invokeObjectInfo, this, this);
        }
    }

    /*
    * If accept data before,don't lock process
    * accept data before 'handlerState' value will be '-1'
    *
    * */
    @Override
    public void requestSuccess() {
        if (handlerState != JOB_ACCEPT_DATA && handlerState != JOB_LOCK_AND_WAIT_RESPONSE) {
            handlerState = JOB_LOCK_AND_WAIT_RESPONSE;
            try {
                this.wait();
            } catch (InterruptedException e) {
                logger.warn("Lock process fail",e);
            }
        }
    }

    /*
    * If accept data,need notify process
    *
    * */
    @Override
    public void responseData() {
        synchronized (this) {
            if (handlerState == JOB_ACCEPT_DATA) return;

            if (handlerState == JOB_LOCK_AND_WAIT_RESPONSE) {
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

    public Type getType() {
        return type;
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

}
