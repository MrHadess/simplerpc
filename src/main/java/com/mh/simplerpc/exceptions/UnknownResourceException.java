/*
*
*   Copyright (c) 2019 MrHadess
*   This source code is licensed under the MIT license found in the
*   LICENSE file in the root directory of this source tree.
*
* */

package com.mh.simplerpc.exceptions;

public class UnknownResourceException extends RuntimeException {

    public UnknownResourceException(String res) {
        super(res);
    }
}
