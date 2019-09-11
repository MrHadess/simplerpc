package com.mh.simplerpc.common;

import io.netty.channel.*;

import java.util.HashMap;
import java.util.LinkedList;

public class ConnectionsToContext {

    private LinkedList<ChannelConnectionStateListener> connectionStateLinkedList = new LinkedList<ChannelConnectionStateListener>();
    private HashMap<String,ChannelHandlerContext> channelHandlerContextMap = new HashMap<String, ChannelHandlerContext>();

    public ChannelHandlerContext getChannelHandlerContext(String channelID) {
        return channelHandlerContextMap.get(channelID);
    }


    public AdapterLinked adapterLinked = new AdapterLinked();
    public class AdapterLinked {
        public void addLast(ChannelConnectionStateListener channelConnectionStateListener) {
            connectionStateLinkedList.addLast(channelConnectionStateListener);
        }
        public void addFirst(ChannelConnectionStateListener channelConnectionStateListener) {
            connectionStateLinkedList.addFirst(channelConnectionStateListener);
        }
        public void add(ChannelConnectionStateListener channelConnectionStateListener) {
            connectionStateLinkedList.add(channelConnectionStateListener);
        }
    }

    public interface ChannelConnectionStateListener {

        void disconnect(String channelID);
        void connect(String channelID);

    }

    public static class ConnectionAdapter extends ChannelHandlerAdapter  {

        private ConnectionsToContext context;

        public ConnectionAdapter(ConnectionsToContext context) {
            this.context = context;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            super.handlerAdded(ctx);
            String channelID = ctx.channel().id().asLongText();
            // focus: before push full message to register this listener,need has new 'channelHandlerContext' object
            context.channelHandlerContextMap.put(channelID,ctx);
            for (ChannelConnectionStateListener item:context.connectionStateLinkedList) {
                item.connect(channelID);
            }
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            super.handlerRemoved(ctx);
            String channelID = ctx.channel().id().asLongText();
            // focus: 'channelHandlerContext' before recovery source,need push full message to register this listener
            for (ChannelConnectionStateListener item:context.connectionStateLinkedList) {
                item.disconnect(channelID);
            }
            context.channelHandlerContextMap.remove(channelID);
        }

    }

    public static class ConnectionAdapter2 extends ChannelInboundHandlerAdapter  {

        private ConnectionsToContext context;

        public ConnectionAdapter2(ConnectionsToContext context) {
            this.context = context;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            super.handlerAdded(ctx);
            String channelID = ctx.channel().id().asLongText();
            // focus: before push full message to register this listener,need has new 'channelHandlerContext' object
            context.channelHandlerContextMap.put(channelID,ctx);
            for (ChannelConnectionStateListener item:context.connectionStateLinkedList) {
                item.connect(channelID);
            }
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            super.handlerRemoved(ctx);
            String channelID = ctx.channel().id().asLongText();
            // focus: 'channelHandlerContext' before recovery source,need push full message to register this listener
            for (ChannelConnectionStateListener item:context.connectionStateLinkedList) {
                item.disconnect(channelID);
            }
            context.channelHandlerContextMap.remove(channelID);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            super.userEventTriggered(ctx, evt);
        }
    }


    public void printChannelHandlerContextMap() {
        System.out.println(channelHandlerContextMap);
    }



}
