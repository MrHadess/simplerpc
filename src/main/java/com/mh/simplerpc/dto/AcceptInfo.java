/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.dto;

import com.google.gson.JsonElement;
import com.mh.simplerpc.ServiceManager;

public class AcceptInfo {

    private CommunicationTypeEnum type;// 业务请求类型
    private String handlerID;// 业务处理ID
    private JsonElement data;// data

    public CommunicationTypeEnum getType() {
        return type;
    }

    public void setType(CommunicationTypeEnum type) {
        this.type = type;
    }

    public String getHandlerID() {
        return handlerID;
    }

    public void setHandlerID(String handlerID) {
        this.handlerID = handlerID;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

    public void setData(Object object) {
        this.data = ServiceManager.getGson().toJsonTree(object);
    }

    @Override
    public String toString() {
        return "AcceptInfo{" +
                "type=" + type +
                ", handlerID='" + handlerID + '\'' +
                ", data=" + data +
                '}';
    }

}
