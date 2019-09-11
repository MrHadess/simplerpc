package com.mh.simplerpc.exceptions;

public class UnknownResourceException extends RuntimeException {

    public UnknownResourceException(String res) {
        super(res);
    }
}
