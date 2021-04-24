package com.miracle.rpc.transport;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Netty 异步调用返回结果包装类
 * @author miracle
 * @date 2021/4/16 22:22
 */
public class ResponseWrapper {
    /**
     * 存储返回结果的阻塞队列
     */
    private final BlockingQueue<Response> responseBlockingQueue = new ArrayBlockingQueue<>(1);

    /**
     * 结果返回时间
     */
    private long responseTime;

    /**
     * 计算该返回结果已过期
     * @return
     */
    public boolean isExpire() {
        Response response = responseBlockingQueue.peek();
        if (response == null) {
            return false;
        }
        long invokeTimeout = response.getInvokeTimeout();
        if ((System.currentTimeMillis() - responseTime) > invokeTimeout) {
            return true;
        }
        return false;
    }

    public static ResponseWrapper of() {
        return new ResponseWrapper();
    }

    public BlockingQueue<Response> getResponseBlockingQueue() {
        return responseBlockingQueue;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
}