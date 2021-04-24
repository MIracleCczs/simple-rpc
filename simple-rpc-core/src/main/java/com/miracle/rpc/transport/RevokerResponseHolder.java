package com.miracle.rpc.transport;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 保存及操作返回结果的数据容器类
 *
 * @author miracle
 * @date 2021/4/16 22:33
 */
@Slf4j
public class RevokerResponseHolder {

    private static final Map<String, ResponseWrapper> RESPONSE_WRAPPER_MAP = Maps.newConcurrentMap();
    private static final ScheduledExecutorService executorService;

    static {
        executorService = new ScheduledThreadPoolExecutor(1, new RemoveExpireThreadFactory("simple-rpc", false));
        // 删除过期的数据
        executorService.scheduleWithFixedDelay(() -> {
            for (Map.Entry<String, ResponseWrapper> entry : RESPONSE_WRAPPER_MAP.entrySet()) {
                boolean expire = entry.getValue().isExpire();
                if (expire) {
                    RESPONSE_WRAPPER_MAP.remove(entry.getKey());
                }
            }
        }, 1, 20, TimeUnit.MILLISECONDS);
    }

    /**
     * 初始化返回结果容器，requestUniqueKey唯一标识本次调用
     *
     * @param requestUniqueKey
     */
    public static void initResponseData(String requestUniqueKey) {
        RESPONSE_WRAPPER_MAP.put(requestUniqueKey, ResponseWrapper.of());
    }

    /**
     * 将Netty调用异步返回结果放入阻塞队列
     *
     * @param response
     */
    public static void putResultValue(Response response) {
        long currentTimeMillis = System.currentTimeMillis();
        ResponseWrapper responseWrapper = RESPONSE_WRAPPER_MAP.get(response.getUniqueKey());
        responseWrapper.setResponseTime(currentTimeMillis);
        responseWrapper.getResponseBlockingQueue().add(response);
        RESPONSE_WRAPPER_MAP.put(response.getUniqueKey(), responseWrapper);
    }

    /**
     * 从阻塞队列中获取异步返回结果
     *
     * @param requestUniqueKey
     * @param timeout
     * @return
     */
    public static Response getValue(String requestUniqueKey, long timeout) {
        ResponseWrapper responseWrapper = RESPONSE_WRAPPER_MAP.get(requestUniqueKey);
        try {
            return responseWrapper.getResponseBlockingQueue().poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("get value error", e);
        } finally {
            RESPONSE_WRAPPER_MAP.remove(requestUniqueKey);
        }
        return null;
    }

    private RevokerResponseHolder() {
    }
}