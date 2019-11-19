package com.mh.simplerpc.common;

import com.mh.simplerpc.exceptions.LoadSSLEngineException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class LoadSSLEngine {

    public static SSLEngine loadToClient(File keyPath,String keyStorePassword,String keyPassword) throws LoadSSLEngineException {
        SSLEngine sslEngine = load(keyPath,keyStorePassword,keyPassword);
        sslEngine.setUseClientMode(true);
        sslEngine.setNeedClientAuth(true);
        return sslEngine;
    }

    public static SSLEngine loadToServer(File keyPath,String keyStorePassword,String keyPassword) throws LoadSSLEngineException {
        SSLEngine sslEngine = load(keyPath,keyStorePassword,keyPassword);
        sslEngine.setUseClientMode(false);
        sslEngine.setNeedClientAuth(true);
        return sslEngine;
    }

    private static SSLEngine load(File keyPath, String keyStorePassword, String keyPassword) throws LoadSSLEngineException {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(keyPath), keyStorePassword.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyPassword.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(keyManagerFactory.getKeyManagers(),trustManagerFactory.getTrustManagers(),null);
            return sslContext.createSSLEngine();
        } catch (KeyStoreException e) {
            throw new LoadSSLEngineException(e);
        } catch (CertificateException e) {
            throw new LoadSSLEngineException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new LoadSSLEngineException(e);
        } catch (IOException e) {
            throw new LoadSSLEngineException(e);
        } catch (UnrecoverableKeyException e) {
            throw new LoadSSLEngineException(e);
        } catch (KeyManagementException e) {
            throw new LoadSSLEngineException(e);
        }
    }


}
