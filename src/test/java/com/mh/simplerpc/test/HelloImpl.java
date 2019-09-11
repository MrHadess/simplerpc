package com.mh.simplerpc.test;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HelloImpl implements Hello {
    public String showTime() {
        System.out.println("HelloImpl use in native");
        return new Date().toString() + "...run in remote";
    }

    public String showTime(String format) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (true) throw new IDontLikeException("haha");
//        if (true) throw new NullPointerException("haha");
//        return new SimpleDateFormat(format).format(new Date());
        return String.format("%s",new SimpleDateFormat(format).format(new Date()));
    }

    public void remoteShowTime() {

    }

    public void testSerializableObjectTime(Date date) {
        System.out.println(String.format("remote serializable time:%s",date));
    }

    public void testTypeListToRemotePrint(List<String> list) {
        for (String item:list) {
            System.out.println("" + item);
        }
    }

    public class IDontLikeException extends RuntimeException {
        private String s;

        public IDontLikeException(String message) {
            super(message);
            this.s = message;
        }
    }

}

// Serializable

