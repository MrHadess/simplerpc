package com.mh.simplerpc.common;

public interface ChannelReadListener<v> {

    void channelOnRead(String channelID, v o);
//    void channelOnRead(Object o);
//    void channelOnRead();

}
