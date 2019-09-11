package com.mh.simplerpc.service.protocol.invocation;

import java.lang.reflect.Type;

public interface InvokeResult {

    void returnType(boolean hasReturn,Object o);
    void runException(Throwable e);

    void setProcessID(String s);
    String getProcessID();
    Type getType();

}
