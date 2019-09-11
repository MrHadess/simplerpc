/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.config;

import com.mh.simplerpc.exceptions.MismatchRESFormatException;

import java.util.regex.Pattern;

public class ProviderEntity {

    private String res;
    private Class<?> toInterface;
    private Class<?> toEntityClass;

    private static Pattern pattern = Pattern.compile("^/(\\w|/)*\\w$");

    public ProviderEntity() { }

    public ProviderEntity(Class<?> toInterface, Class<?> toEntityClass) {
        this.res = toInterface.getName();
        this.toInterface = toInterface;
        this.toEntityClass = toEntityClass;
    }

    /**
     * res must be URL style
     *
     * */
    public ProviderEntity(String res, Class<?> toInterface, Class<?> toEntityClass) throws MismatchRESFormatException {
        if (!pattern.matcher(res).matches()) {
            throw new MismatchRESFormatException("params res is not url style");
        }
        this.res = res;
        this.toInterface = toInterface;
        this.toEntityClass = toEntityClass;
    }


    public String getRes() {
        return res;
    }

    public Class<?> getToInterface() {
        return toInterface;
    }

    public Class<?> getToEntityClass() {
        return toEntityClass;
    }


    @Override
    public String toString() {
        return "ProviderEntity{" +
                "res='" + res + '\'' +
                ", toInterface=" + toInterface +
                ", toEntityClass=" + toEntityClass +
                '}';
    }

}
