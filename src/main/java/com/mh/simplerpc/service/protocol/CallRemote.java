package com.mh.simplerpc.service.protocol;

import com.mh.simplerpc.pojo.InvokeObjectInfo;
import com.mh.simplerpc.service.protocol.invocation.InvokeResult;

public interface CallRemote {

    void call(InvokeObjectInfo invokeObjectInfo, InvokeResult invokeResult,Thread thread);
    void recoveryCallObject(String processID);

}
