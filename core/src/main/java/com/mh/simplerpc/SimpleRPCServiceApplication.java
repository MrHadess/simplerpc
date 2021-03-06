/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc;

import com.mh.simplerpc.exceptions.LoadSSLEngineException;
import com.mh.simplerpc.exceptions.MismatchRESFormatException;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/*
* use to command startup service
*
* */
public class SimpleRPCServiceApplication {

    private static Logger logger = LoggerFactory.getLogger(SimpleRPCServiceApplication.class);

    private static boolean startCommand = false;
    private static ClassLoader commandClassLoader = null;

    public static boolean isStartCommand() {
        return startCommand;
    }

    public static ClassLoader getCommandClassLoader() {
        return commandClassLoader;
    }

    /*
    *
    * args is zero use default startup plan
    * args length = 1 trust be is config file local
    * args length = 2 and [1] = '-code' then will be start else program to create service
    *
    * */
    public static void main(String[] args) throws Exception {

        startCommand = true;

        // first load default config file
//        String classLocal = Thread.currentThread().getContextClassLoader().getResource("/").getFile();
        String classLocal = SimpleRPCServiceApplication.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        if (classLocal.endsWith("simplerpc.jar")){
            classLocal = classLocal.substring(0,classLocal.length() - 13);
        }

        File classFolder = new File(classLocal + File.separator);
//        URL[] urls = new URL[]{classFolder.toURI().toURL()};
//        Thread.currentThread().setContextClassLoader(new URLClassLoader(urls));
        commandClassLoader = new URLClassLoader(classPathToGroup(classFolder));
        Thread.currentThread().setContextClassLoader(commandClassLoader);

//        File file = new File(classLocal + File.separator + "simple-rpc.xml");


        File file = null;
        switch (args.length) {
            case 0:
                file = new File(classLocal + File.separator + "simple-rpc.xml");
                break;
            case 1:
                file = new File(args[0]);
                break;
        }


        ServiceManager serviceManager;
        try {
            serviceManager = createService(file);
        } catch (ClassNotFoundException e) {
            logger.warn("Startup fail,lost class",e);
            return;
        } catch (MismatchRESFormatException e) {
            logger.warn("Startup fail,unsupported res format!",e);
            return;
        } catch (FileNotFoundException e) {
            logger.warn(String.format("Unknown file:%s",file));
            throw e;
        }

        // Exit logic
        logger.warn("Input key 'q' exit program");

        Scanner scanner = new Scanner(System.in);
        boolean holdState = true;
        while (holdState) {
            if (!"q".equals(scanner.next())) {
                logger.warn("Unsupported input key value");
                continue;
            }

            holdState = false;
            serviceManager.shutdown();
        }

        TimeUnit.SECONDS.sleep(7);
        System.exit(0);

    }

    public static ServiceManager createService(File configFileLocal) throws DocumentException, FileNotFoundException, ClassNotFoundException, MismatchRESFormatException, LoadSSLEngineException {
        ServiceConfig serviceConfig = LoadConfigFile.loadConfigToFile(configFileLocal);
        ServiceManager serviceManager = new ServiceManager(serviceConfig);
        serviceManager.startup();
        return serviceManager;
    }

    private static URL[] classPathToGroup(File classFolder) throws MalformedURLException {
        ArrayList<URL> fileList = new ArrayList<URL>();
        fileList.add(classFolder.toURI().toURL());
        String[] folderList = classFolder.list();
        if (folderList == null) return fileList.toArray(new URL[]{});
        for (String item:folderList) {
            File tempFile = new File(classFolder + File.separator + item);
//            logger.info(tempFile.toString());
            if (item.endsWith(".jar") && tempFile.isFile()) {
                fileList.add(tempFile.toURI().toURL());
            }
        }

        return fileList.toArray(new URL[]{});
    }

}
