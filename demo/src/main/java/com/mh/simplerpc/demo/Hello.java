package com.mh.simplerpc.demo;

import java.util.Date;
import java.util.List;

public interface Hello {

    String showTime(String format) throws HelloImpl.IDoNotLikeException;

}
