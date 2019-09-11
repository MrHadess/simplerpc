/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc;

import com.mh.simplerpc.config.ConsumerEntity;
import com.mh.simplerpc.config.ProviderEntity;
import com.mh.simplerpc.exceptions.MismatchRESFormatException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

public class LoadConfigFile {

    private static Logger logger = LoggerFactory.getLogger(LoadConfigFile.class);

    public static ServiceConfig loadConfigToFile(File file) throws FileNotFoundException, ClassNotFoundException, DocumentException, MismatchRESFormatException {
        if (file == null || !file.exists()) {
            logger.info("file is null or is empty!");
            throw new FileNotFoundException("file is null or is empty!");
        }

        return docResolve(file);
    }

    private static ServiceConfig docResolve(File file) throws DocumentException,ClassNotFoundException,MismatchRESFormatException {
        //加载文件
        SAXReader saxReader = new SAXReader();
//        saxReader.setEntityResolver((publicId, systemId) -> {
//            InputSource inputSource = new InputSource(new ByteArrayInputStream("".getBytes()));
//            inputSource.setPublicId(publicId);
//            inputSource.setSystemId(systemId);
//            return inputSource;
//        });
        Document document = saxReader.read(file);
        Element rootElement = document.getRootElement();
        Element providerListElement = rootElement.element("provider-list");
        Element consumerListElement = rootElement.element("consumer-list");

        ServiceConfig.Builder serviceConfigBuilder = new ServiceConfig.Builder();

        ClassLoader classLoader = getClassLoader();

        loadCommonConfig(rootElement,serviceConfigBuilder);
        loadProviderListConfig(providerListElement,serviceConfigBuilder,classLoader);
        loadConsumerListConfig(consumerListElement,serviceConfigBuilder,classLoader);

        return serviceConfigBuilder.build();
    }

    private static void loadCommonConfig(Element rootElement,ServiceConfig.Builder builder) throws NumberFormatException {
        Element elementRemoteIP = rootElement.element("remote-ip");
        Element elementRemotePort = rootElement.element("remote-port");
        Element elementOAuthCode = rootElement.element("oauth-code");
        Element elementJobMode = rootElement.element("job-mode");
        Element elementTryConnectNum = rootElement.element("try-connect-num");
        Element elementTryRecoveryConnectNum = rootElement.element("try-recovery-connect-num");

        if (elementRemoteIP != null) { builder.setRemoteIP(elementRemoteIP.getText()); }
        if (elementRemotePort != null) { builder.setRemotePort(Integer.valueOf(elementRemotePort.getText())); }
        if (elementOAuthCode != null) { builder.setOAuthCode(elementOAuthCode.getText()); }
        if (elementJobMode != null) { builder.setJobMode(Integer.valueOf(elementJobMode.getText())); }
        if (elementTryConnectNum != null) { builder.setTryConnectNum(Integer.valueOf(elementTryConnectNum.getText())); }
        if (elementTryRecoveryConnectNum != null) { builder.setTryRecoveryConnectNum(Integer.valueOf(elementTryRecoveryConnectNum.getText())); }

    }

    private static void loadProviderListConfig(Element providerListElement,ServiceConfig.Builder builder,ClassLoader classLoader) throws ClassNotFoundException,MismatchRESFormatException {
        if (providerListElement == null) return;

        Iterator<Element> elementIterator = providerListElement.elementIterator("provider");
        while (elementIterator.hasNext()) {
            Element itemElement = elementIterator.next();
            String res = itemElement.attributeValue("res");
            String toInterfaceClassStr = itemElement.attributeValue("interface");
            String toEntityClassStr = itemElement.attributeValue("entity");

            if (toInterfaceClassStr == null || toEntityClassStr == null) {
                logger.info("provider list has unknown attribute value to [interface,entity]");
                continue;
            }

            ProviderEntity providerEntity = null;

            Class<?> toInterface;
            Class<?> toEntity;
            try {
//                toInterface = Class.forName(toInterfaceClassStr);
//                toInterface = Class.forName(toInterfaceClassStr,true,Thread.currentThread().getContextClassLoader());
                toInterface = Class.forName(toInterfaceClassStr,true,classLoader);
            } catch (ClassNotFoundException e) {
                logger.warn(String.format("provider load fail please check interface config: %s",toInterfaceClassStr));
                throw e;
            }
            try {
//                toEntity = Class.forName(toEntityClassStr);
//                toEntity = Class.forName(toEntityClassStr,true,Thread.currentThread().getContextClassLoader());
                toEntity = Class.forName(toEntityClassStr,true,classLoader);
            } catch (ClassNotFoundException e) {
                logger.warn(String.format("provider load fail please check entity config: %s",toEntityClassStr));
                throw e;
            }

            if (res == null) {
                providerEntity = new ProviderEntity(toInterface,toEntity);
            } else {
                providerEntity = new ProviderEntity(res,toInterface,toEntity);
            }

            builder.addProviderList(providerEntity);
//            builder.addProviderList(new ProviderEntity(res,toInterface,toEntity));

        }

    }

    private static void loadConsumerListConfig(Element consumerListElement,ServiceConfig.Builder builder,ClassLoader classLoader) throws ClassNotFoundException,MismatchRESFormatException {
        if (consumerListElement == null) return;

        Iterator<Element> elementIterator = consumerListElement.elementIterator("consumer");
        while (elementIterator.hasNext()) {
            Element itemElement = elementIterator.next();
            String res = itemElement.attributeValue("res");
            String toInterfaceClassStr = itemElement.attributeValue("interface");

            if (toInterfaceClassStr == null) {
                logger.info("provider list has unknown attribute value to [interface]");
                continue;
            }

            ConsumerEntity consumerEntity;
            Class<?> toInterface;
            try {
//                toInterface = Class.forName(toInterfaceClassStr);
                toInterface = Class.forName(toInterfaceClassStr,true,classLoader);
            } catch (ClassNotFoundException e) {
                logger.warn(String.format("provider load fail please check interface config: %s",toInterfaceClassStr));
                throw e;
            }

            if (res == null) {
                consumerEntity = new ConsumerEntity(toInterface);
            } else {
                consumerEntity = new ConsumerEntity(res,toInterface);
            }

            builder.addConsumerList(consumerEntity);

        }

    }

    private static ClassLoader getClassLoader() {
        // first use to thread classloader
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            // then use default classloader
            classLoader = LoadConfigFile.class.getClassLoader();
        }
        return classLoader;
    }


}
