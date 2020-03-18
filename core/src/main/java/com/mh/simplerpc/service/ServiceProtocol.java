/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service;

import com.mh.simplerpc.dto.ProviderInfo;
import com.mh.simplerpc.exceptions.DoesNotConnectException;
import com.mh.simplerpc.pojo.InvokeObjectInfo;
import com.mh.simplerpc.pojo.InvokeObjectResultInfo;

import java.util.List;

public interface ServiceProtocol {

    // use to native handler
    interface Invocation {
        void updateIsConnectState(boolean state);
        void acceptProviderServices(List<ProviderInfo> providerInfoList);
        void acceptInvokeObject(String processID, InvokeObjectInfo invokeObjectInfo);
        void acceptInvokeObjectResult(String processID, InvokeObjectResultInfo invokeObjectResultInfo);
    }

    // use to send message
    interface Result {
        void remoteInvokeObject(String processID, InvokeObjectInfo invokeObjectInfo) throws DoesNotConnectException;
        void remoteInvokeObjectResult(String processID, InvokeObjectResultInfo invokeObjectResultInfo) throws DoesNotConnectException;
    }

}
