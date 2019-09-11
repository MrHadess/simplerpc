package com.mh.simplerpc.test;

import com.google.gson.Gson;
import org.junit.Test;



public class test4 {

    @Test
    public void test4A() throws Exception {
        Gson json = new Gson();
        NullPointerException stackException = new NullPointerException("Unknown");
        String data = json.toJson(new Exception("haha",stackException));
        System.out.println(data);
        throw json.fromJson(data,Exception.class);

    }














}
