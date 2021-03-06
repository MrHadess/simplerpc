/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service.protocol;

import com.mh.simplerpc.pojo.InvokeObjectInfo;
import com.mh.simplerpc.service.protocol.invocation.InvokeResult;
import com.mh.simplerpc.service.protocol.invocation.InvokeState;

public interface CallRemote {

    void call(InvokeObjectInfo invokeObjectInfo, InvokeResult invokeResult, InvokeState invokeState);

}
