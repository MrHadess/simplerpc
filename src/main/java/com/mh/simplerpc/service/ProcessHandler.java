/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service;

import com.google.gson.reflect.TypeToken;
import com.mh.simplerpc.ServiceConfig;
import com.mh.simplerpc.ServiceManager;
import com.mh.simplerpc.common.ChannelReadListener;
import com.mh.simplerpc.config.ConsumerEntity;
import com.mh.simplerpc.config.ProviderEntity;
import com.mh.simplerpc.dto.AcceptInfo;
import com.mh.simplerpc.dto.CommunicationTypeEnum;
import com.mh.simplerpc.dto.ConsumerInfo;
import com.mh.simplerpc.dto.ProviderInfo;
import com.mh.simplerpc.exceptions.DoesNotConnectException;
import com.mh.simplerpc.pojo.InvokeObjectInfo;
import com.mh.simplerpc.pojo.InvokeObjectResultInfo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessHandler implements ChannelReadListener<AcceptInfo>,ServiceProtocol.Result {

    private ServiceConfig serviceConfig;
    private ServiceProtocol.Invocation serInvocation;

    private static Logger logger = LoggerFactory.getLogger(ProcessHandler.class);

    @Deprecated
    public ProcessHandler(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public ProcessHandler(ServiceConfig serviceConfig,ServiceProtocol.Invocation serInvocation) {
        this.serviceConfig = serviceConfig;
        this.serInvocation = serInvocation;
    }

    private ChannelHandlerContext channelHandlerContext;

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
        serInvocation.updateIsConnectState(true);
    }

    public boolean tryRecoveryContext(String channelID) {
        serInvocation.updateIsConnectState(false);
        if (channelHandlerContext == null) return false;
        if (channelHandlerContext.channel().id().asLongText().equals(channelID)) {
            // recovery resource
            channelHandlerContext = null;
            return true;
        }
        return false;
    }

    public void channelOnRead(String channelID, AcceptInfo acceptInfo) {
        if (acceptInfo.getType() == null) return;

        switch (acceptInfo.getType()) {
            case ConsumerServices:
                List<ConsumerInfo> consumerInfoList = ServiceManager.getGson().fromJson(acceptInfo.getData(),new TypeToken<List<ConsumerInfo>>(){}.getType());
                logger.info("AcceptConsumerServices",consumerInfoList);
                // nothing to do,do tell unsupported services
                break;
            case ProviderServices:
                List<ProviderInfo> providerInfoList = ServiceManager.getGson().fromJson(acceptInfo.getData(),new TypeToken<List<ProviderInfo>>(){}.getType());
                logger.info("AcceptProviderServices",providerInfoList);
                // save this map,hasn't this service match to use
                serInvocation.acceptProviderServices(providerInfoList);
                break;
            case InvokeObject:
                String handlerID = acceptInfo.getHandlerID();
                InvokeObjectInfo invokeObjectInfo = ServiceManager.getGson().fromJson(acceptInfo.getData(),InvokeObjectInfo.class);
                logger.info("AcceptInvokeObject",invokeObjectInfo.toSimpleInfo());
                if (logger.isTraceEnabled()) {
                    logger.trace("AcceptInvokeObject-data", Arrays.asList(invokeObjectInfo.getArgs()));
                }
                serInvocation.acceptInvokeObject(handlerID,invokeObjectInfo);
                break;
            case InvokeObjectResult:
                String handlerID2 = acceptInfo.getHandlerID();
                InvokeObjectResultInfo invokeObjectResultInfo = ServiceManager.getGson().fromJson(acceptInfo.getData(),InvokeObjectResultInfo.class);
                logger.info("AcceptInvokeObjectResult",invokeObjectResultInfo.toSimpleInfo());
                if (logger.isTraceEnabled()) {
                    logger.trace("AcceptInvokeObjectResult-data", invokeObjectResultInfo.getArg());
                }
                serInvocation.acceptInvokeObjectResult(handlerID2,invokeObjectResultInfo);
                break;
        }

    }

    // bind success first step
    public void authSuccess() {


        // send provider/consumer info
        Channel channel = channelHandlerContext.channel();
        List<ProviderEntity> providerEntityList = serviceConfig.getProviderConfigList();
        if (providerEntityList.size() > 0) {
            List<ProviderInfo> providerInfoList = new ArrayList<ProviderInfo>();
            for (ProviderEntity item:providerEntityList) {
                ProviderInfo providerInfo = new ProviderInfo();
                providerInfo.setRes(item.getRes());
                providerInfoList.add(providerInfo);
            }
            AcceptInfo sendProviderInfo = new AcceptInfo();
            sendProviderInfo.setType(CommunicationTypeEnum.ProviderServices);
            sendProviderInfo.setData(providerInfoList);
            channel.writeAndFlush(sendProviderInfo);
        }


        List<ConsumerEntity> consumerEntityList = serviceConfig.getConsumerConfigList();
        if (consumerEntityList.size() > 0) {
            List<ConsumerInfo> consumerInfoList = new ArrayList<ConsumerInfo>();
            for (ConsumerEntity item:consumerEntityList) {
                ConsumerInfo consumerInfo = new ConsumerInfo(item.getRes());
                consumerInfoList.add(consumerInfo);
            }
            AcceptInfo sendConsumerInfo = new AcceptInfo();
            sendConsumerInfo.setType(CommunicationTypeEnum.ConsumerServices);
            sendConsumerInfo.setData(consumerInfoList);
            channel.writeAndFlush(sendConsumerInfo);
        }


    }


    public void remoteInvokeObject(String processID, InvokeObjectInfo invokeObjectInfo) throws DoesNotConnectException {
        logger.info("remoteInvokeObject",invokeObjectInfo.toSimpleInfo());
        if (logger.isTraceEnabled()) {
            logger.trace("remoteInvokeObject-data", Arrays.asList(invokeObjectInfo.getArgs()));
        }
        if (channelHandlerContext == null) throw new DoesNotConnectException();
        AcceptInfo acceptInfo = new AcceptInfo();
        acceptInfo.setType(CommunicationTypeEnum.InvokeObject);
        acceptInfo.setHandlerID(processID);
        acceptInfo.setData(invokeObjectInfo);
        channelHandlerContext.channel().writeAndFlush(acceptInfo);
    }

    public void remoteInvokeObjectResult(String processID, InvokeObjectResultInfo invokeObjectResultInfo) throws DoesNotConnectException {
        logger.info("remoteInvokeObjectResult",invokeObjectResultInfo.toSimpleInfo());
        if (logger.isTraceEnabled()) {
            logger.trace("remoteInvokeObjectResult-data", invokeObjectResultInfo.getArg());
        }
        if (channelHandlerContext == null) throw new DoesNotConnectException();
        AcceptInfo acceptInfo = new AcceptInfo();
        acceptInfo.setType(CommunicationTypeEnum.InvokeObjectResult);
        acceptInfo.setHandlerID(processID);
        acceptInfo.setData(invokeObjectResultInfo);
        channelHandlerContext.channel().writeAndFlush(acceptInfo);
    }
}
