/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.service;

import io.netty.channel.ChannelHandlerContext;

public interface ServiceMessage {
    // 连接成功 取得 channel 对象
    /**
     * @return confirm connect state
     *
     * */
    boolean connectSuccess(ServiceControl serviceControl,ChannelHandlerContext channelHandlerContext);

    // 存在一客户端断开连接
    void disconnect(String channelID);

}
