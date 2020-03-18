package com.mh.simplerpc.demo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HelloImpl implements Hello {

    public String showTime(String format) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        You can throw exception
//        throw new IDoNotLikeException("message");
        return String.format("%s",new SimpleDateFormat(format).format(new Date()));
    }

    public class IDoNotLikeException extends RuntimeException {
        private String s;

        public IDoNotLikeException(String message) {
            super(message);
            this.s = message;
        }
    }

}

