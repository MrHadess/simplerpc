package com.mh.simplerpc.dto;

public class AuthResult {

    private boolean authSuccess;

    public boolean isAuthSuccess() {
        return authSuccess;
    }

    public void setAuthSuccess(boolean authSuccess) {
        this.authSuccess = authSuccess;
    }

    @Override
    public String toString() {
        return "AuthResult{" +
                "authSuccess=" + authSuccess +
                '}';
    }
}
