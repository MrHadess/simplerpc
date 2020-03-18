/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

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
