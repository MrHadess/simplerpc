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
