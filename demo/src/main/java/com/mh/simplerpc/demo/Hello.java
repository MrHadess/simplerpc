package com.mh.simplerpc.demo;

import java.util.Date;
import java.util.List;

public interface Hello {
    String showTime();
    String showTime(String format) throws HelloImpl.IDontLikeException;
    void remoteShowTime();
    void testSerializableObjectTime(Date date);
    void testTypeListToRemotePrint(List<String> list);
    interface Child {
        String showTime();
    }
}
