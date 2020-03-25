package com.mh.simplerpc.service.protocol.invocation;

/*
* Get invoke state,use to
*
* */
public interface InvokeState {

    /*
    * Just run once
    * */
    void requestSuccess();

    /*
    * Just run once
    * */
    void responseData();

}
