/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service.protocol.invocation;

import java.lang.reflect.Type;

public interface InvokeResult {

    void returnType(boolean hasReturn,Object o);
    void runException(Throwable e);

    void setProcessID(String s);
    String getProcessID();
    Type getType();

}
