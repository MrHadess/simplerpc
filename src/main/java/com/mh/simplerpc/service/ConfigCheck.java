package com.mh.simplerpc.service;

import com.mh.simplerpc.config.ConsumerEntity;
import com.mh.simplerpc.config.ProviderEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Templates;
import java.util.Iterator;
import java.util.List;

public class ConfigCheck {

    private static Logger logger = LoggerFactory.getLogger(ConfigCheck.class);

    public static void checkIsInterface(List<ConsumerEntity> consumerEntityList) {
        Iterator<ConsumerEntity> iterator = consumerEntityList.iterator();
        ConsumerEntity tempConsumerEntity;
        while (iterator.hasNext()) {
            if ((tempConsumerEntity = iterator.next()).getToInterface().isInterface()) continue;

            logger.warn(
                    String.format("%s is not interface",tempConsumerEntity.getToInterface().getName())
            );

            iterator.remove();
        }
    }

    public static void checkIsImpl(List<ProviderEntity> providerEntityList) {
        Iterator<ProviderEntity> iterator = providerEntityList.iterator();
        ProviderEntity tempProviderEntity;
        while (iterator.hasNext()) {
            tempProviderEntity = iterator.next();

            if (!tempProviderEntity.getToInterface().isInterface()) {
                logger.warn(
                        String.format("%s is not interface", tempProviderEntity.getRes())
                );

                iterator.remove();
                continue;
            }

//            if (!tempProviderEntity.getToEntityClass().isInstance(tempProviderEntity.getToInterface())) {
//            if (!tempProviderEntity.getToInterface().isInstance(tempProviderEntity.getToEntityClass())) {
            if (!tempProviderEntity.getToInterface().isAssignableFrom(tempProviderEntity.getToEntityClass())) {
                logger.warn(
                        String.format("%s is not instance", tempProviderEntity.getRes())
                );

                iterator.remove();
                continue;
            }

        }
    }


}
