package com.mh.simplerpc.config;

import java.io.File;

public class EncryptConnectInfo {

    private File keyPath;
    private String keyStorePassword;
    private String keyPassword;

    /**
     * Constructor
     * key password use to same key store password
     *
     * */
    public EncryptConnectInfo(File keyPath, String keyStorePassword) {
        this.keyPath = keyPath;
        this.keyStorePassword = keyStorePassword;
    }

    public EncryptConnectInfo(File keyPath, String keyStorePassword, String keyPassword) {
        this.keyPath = keyPath;
        this.keyStorePassword = keyStorePassword;
        this.keyPassword = keyPassword;
    }

    public File getKeyPath() {
        return keyPath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    @Override
    public String toString() {
        return "EncryptConnectInfo{" +
                "keyPath=" + keyPath +
                ", keyStorePassword='" + keyStorePassword + '\'' +
                ", keyPassword='" + keyPassword + '\'' +
                '}';
    }

    public static class Builder {
        private File keyPath;
        private String keyStorePassword;
        private String keyPassword;

        public Builder setKeyPath(File keyPath) {
            this.keyPath = keyPath;
            return this;
        }

        public Builder setKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
            return this;
        }

        public Builder setKeyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
            return this;
        }

        public EncryptConnectInfo build() {
            return new EncryptConnectInfo(keyPath,keyStorePassword,keyPassword);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "keyPath=" + keyPath +
                    ", keyStorePassword='" + keyStorePassword + '\'' +
                    ", keyPassword='" + keyPassword + '\'' +
                    '}';
        }
    }

}
