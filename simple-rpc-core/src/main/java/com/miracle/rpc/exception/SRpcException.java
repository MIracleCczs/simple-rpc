package com.miracle.rpc.exception;

/**
 * 自定义异常
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/23 19:25
 */
public class SRpcException extends RuntimeException{

    public SRpcException() {
        super();
    }

    public SRpcException(String message) {
        super(message);
    }

    public SRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public SRpcException(Throwable cause) {
        super(cause);
    }

    protected SRpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
