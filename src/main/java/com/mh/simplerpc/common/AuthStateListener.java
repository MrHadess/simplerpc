/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.common;

import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;

public interface AuthStateListener {

    void authSuccessDisconnect(ChannelHandlerContext channelHandlerContext,InetSocketAddress localAddress,InetSocketAddress remoteAddress);

}
