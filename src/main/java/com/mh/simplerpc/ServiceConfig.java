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
import com.mh.simplerpc.service.ConfigCheck;

import java.util.ArrayList;
import java.util.List;

/*
* service config
*
* */
public class ServiceConfig {

    public static final int JOB_MODE_HYBRID = 0;
    public static final int JOB_MODE_LISTENER = 1;
    public static final int JOB_MODE_CONNECT = 2;

    private String remoteIP;
    private int remotePort = 28095;//will be define listener port

    private String OAuthCode = "0000-0000-0000-0000-0000";

    private int jobMode = 0;// 0 all,1 provider,2 consumer
    private int tryConnectNum = 0;
    private int tryRecoveryConnectNum = 10;// default value 10,will be 1sec to try again

    private List<ProviderEntity> providerConfigList = new ArrayList<ProviderEntity>();
    private List<ConsumerEntity> consumerConfigList = new ArrayList<ConsumerEntity>();


    private ServiceConfig(Builder builder) {
        remoteIP = builder.remoteIP;
        remotePort = builder.remotePort;
        jobMode = builder.jobMode;
        tryConnectNum = builder.tryConnectNum;
        tryRecoveryConnectNum = builder.tryRecoveryConnectNum;
        OAuthCode = builder.OAuthCode;
        consumerConfigList = builder.consumerConfigList;
        providerConfigList = builder.providerConfigList;
    }


    public String getRemoteIP() {
        return remoteIP;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getOAuthCode() {
        return OAuthCode;
    }

    public List<ProviderEntity> getProviderConfigList() {
        return providerConfigList;
    }

    public List<ConsumerEntity> getConsumerConfigList() {
        return consumerConfigList;
    }

    public int getJobMode() {
        return jobMode;
    }

    public int getTryConnectNum() {
        return tryConnectNum;
    }

    public int getTryRecoveryConnectNum() {
        return tryRecoveryConnectNum;
    }

    @Override
    public String toString() {
        return "ServiceConfig{" +
                "remoteIP='" + remoteIP + '\'' +
                ", remotePort=" + remotePort +
                ", OAuthCode='" + OAuthCode + '\'' +
                ", jobMode=" + jobMode +
                ", tryConnectNum=" + tryConnectNum +
                ", tryRecoveryConnectNum=" + tryRecoveryConnectNum +
                ", providerConfigList=" + providerConfigList +
                ", consumerConfigList=" + consumerConfigList +
                '}';
    }

    public static class Builder {

        private String remoteIP;
        private int remotePort = 28095;//will be define listener port

        private String OAuthCode = "0000-0000-0000-0000-0000";

        private int jobMode = 0;// 0 all,1 provider,2 consumer
        private int tryConnectNum = 0;
        private int tryRecoveryConnectNum = 12;// default value 12,will be 10sec to try again

        private List<ProviderEntity> providerConfigList = new ArrayList<ProviderEntity>();
        private List<ConsumerEntity> consumerConfigList = new ArrayList<ConsumerEntity>();

//        public Builder(String remoteIP) {
//            this.remoteIP = remoteIP;
//        }

        public Builder() { }

        public Builder setRemoteIP(String remoteIP) {
            this.remoteIP = remoteIP;
            return this;
        }

        public Builder setRemotePort(int remotePort) {
            this.remotePort = remotePort;
            return this;
        }

        public Builder setOAuthCode(String OAuthCode) {
            this.OAuthCode = OAuthCode;
            return this;
        }

        public Builder setJobMode(int jobMode) {
            this.jobMode = jobMode;
            return this;
        }

        public Builder setTryConnectNum(int tryConnectNum) {
            this.tryConnectNum = tryConnectNum;
            return this;
        }

        public Builder setTryRecoveryConnectNum(int tryRecoveryConnectNum) {
            this.tryRecoveryConnectNum = tryRecoveryConnectNum;
            return this;
        }

        public Builder setProviderConfigList(List<ProviderEntity> providerConfigList) {
            this.providerConfigList = providerConfigList;
            return this;
        }

        public Builder setConsumerConfigList(List<ConsumerEntity> consumerConfigList) {
            this.consumerConfigList = consumerConfigList;
            return this;
        }

        public Builder addProviderList(ProviderEntity providerEntity) {
            if (providerConfigList == null) return this;
            this.providerConfigList.add(providerEntity);
            return this;
        }

        public Builder addConsumerList(ConsumerEntity consumerEntity) {
            if (consumerConfigList == null) return this;
            this.consumerConfigList.add(consumerEntity);
            return this;
        }

        public ServiceConfig build() {
            ConfigCheck.checkIsInterface(consumerConfigList);
            ConfigCheck.checkIsImpl(providerConfigList);
            return new ServiceConfig(this);
        }
    }
}
