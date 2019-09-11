package com.mh.simplerpc.exceptions;

public class RemoteInvokeException extends RuntimeException {

    private String remoteInvokeExceptionStack;

    public RemoteInvokeException(String remoteInvokeExceptionStack) {
        super();
        this.remoteInvokeExceptionStack = remoteInvokeExceptionStack;

    }

    public RemoteInvokeException(Throwable cause) {
        super(cause);
    }

    public String getRemoteInvokeExceptionStack() {
        return remoteInvokeExceptionStack;
    }

    @Override
    public String toString() {
        return "RemoteInvokeException{" +
                "remoteInvokeExceptionStack='" + remoteInvokeExceptionStack + '\'' +
                '}';
    }

}
