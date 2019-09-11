package com.mh.simplerpc.service;

import com.mh.simplerpc.common.ChannelReadListener;
import com.mh.simplerpc.dto.AcceptInfo;

public interface ServiceControl {

    // 连接成功后 将执行信息交移工作 (当未交移成功时间启用自身的业务未转移协议用于得知服务为未被受理)
    void moveMsgAcceptObject(ChannelReadListener<AcceptInfo> acceptInfoChannelReadListener);


//    void start();
//    void stop();
//
//    interface state {
//        void connectSuccess();//连接成功 取得通信的唯一对象
//        void sss();
//    }

}
