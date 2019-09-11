package com.mh.simplerpc.pojo;

import java.util.Arrays;

public class InvokeObjectInfo {

    private String res;
    private String methodName;
    private String[] methodParam;
    private String[] args;

    public String getRes() {
        return res;
    }

    public void setRes(String res) {
        this.res = res;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getMethodParam() {
        return methodParam;
    }

    public void setMethodParam(String[] methodParam) {
        this.methodParam = methodParam;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "InvokeNeed{" +
                "res='" + res + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodParam=" + Arrays.toString(methodParam) +
                ", args=" + Arrays.toString(args) +
                '}';
    }

    public String toSimpleInfo() {
        return "InvokeNeed{" +
                "res='" + res + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodParam=" + Arrays.toString(methodParam) +
                '}';
    }


}
