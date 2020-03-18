/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.common;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Type;


/*
*
* 以JSON数据结构为主要通信机制
* 利用多层数据嵌套，进行数据处理
* 当前层级进行初阶数据类型解码处理
*
*
* */
public class ObjectInDataHandler extends ChannelInboundHandlerAdapter {

    private static Gson json = new Gson();
    private Class dataModelClass;
    private ChannelReadListener channelReadListener;

    public ObjectInDataHandler(Class dataModelClass, ChannelReadListener channelReadListener) {
        this.dataModelClass = dataModelClass;
        this.channelReadListener = channelReadListener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        super.channelRead(ctx, msg);

        if (msg == null || !(msg instanceof String)) {
            super.channelRead(ctx, msg);
            return;
        }
//        if (msg == null || !(msg instanceof String)) return;
        try {
            //pull data
            Object object = json.fromJson((String) msg,dataModelClass);
            channelReadListener.channelOnRead(ctx.channel().id().asLongText(),object);

        } catch (JsonSyntaxException e) {
            e.printStackTrace();

            //数据解析失败时将关闭相关连接通道
            ctx.close();
        }


    }
}
