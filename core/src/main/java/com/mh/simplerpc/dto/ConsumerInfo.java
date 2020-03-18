/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.dto;

/*
* 消费者
*
* 检验凭证将使用res作为服务识别凭证
*
* 缺陷注意事项:自定义res时可能会发生资源定义一致，但提供的服务与消费所需的服务不一致，导致服务不可用
*
* */
public class ConsumerInfo {

    private String res;

    public ConsumerInfo() {
    }

    public ConsumerInfo(String res) {
        this.res = res;
    }

    public String getRes() {
        return res;
    }

    public void setRes(String res) {
        this.res = res;
    }

    @Override
    public String toString() {
        return "ConsumerInfo{" +
                "res='" + res + '\'' +
                '}';
    }

}
