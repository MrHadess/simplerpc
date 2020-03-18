/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.common;

public interface ChannelReadListener<v> {

    void channelOnRead(String channelID, v o);
//    void channelOnRead(Object o);
//    void channelOnRead();

}
