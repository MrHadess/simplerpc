package com.mh.simplerpc.config;

import com.mh.simplerpc.exceptions.MismatchRESFormatException;

import java.util.regex.Pattern;

public class ConsumerEntity {

    private String res;
    private Class<?> toInterface;

    private static Pattern pattern = Pattern.compile("^/(\\w|/)*\\w$");

    public ConsumerEntity() { }

    public ConsumerEntity(Class<?> toInterface) {
        this.res = toInterface.getName();
        this.toInterface = toInterface;
    }

    public ConsumerEntity(String res, Class<?> toInterface) throws MismatchRESFormatException {
        if (!pattern.matcher(res).matches()) {
            throw new MismatchRESFormatException("params res is not url style");
        }
        this.res = res;
        this.toInterface = toInterface;
    }

    public String getRes() {
        return res;
    }

    public Class<?> getToInterface() {
        return toInterface;
    }


    @Override
    public String toString() {
        return "ConsumerEntity{" +
                "res='" + res + '\'' +
                ", toInterface=" + toInterface +
                '}';
    }

}
