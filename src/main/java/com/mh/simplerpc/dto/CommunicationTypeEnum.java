/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.dto;


public enum CommunicationTypeEnum {

    ClientAuth,
    AuthResult,
    ConsumerServices,//消费的服务
    ProviderServices,//提供的服务
    InvokeObject,
    InvokeObjectResult,
    RequestReject

}
