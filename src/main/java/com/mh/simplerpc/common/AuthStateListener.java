package com.mh.simplerpc.common;

import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;

public interface AuthStateListener {

    void authSuccessDisconnect(ChannelHandlerContext channelHandlerContext,InetSocketAddress localAddress,InetSocketAddress remoteAddress);

}
