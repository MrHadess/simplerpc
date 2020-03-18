package com.mh.simplerpc.dto;

public class ClientAuthInfo {

    private String authCode;

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    @Override
    public String toString() {
        return "ClientAuthInfo{" +
                "authCode='" + authCode + '\'' +
                '}';
    }
}
