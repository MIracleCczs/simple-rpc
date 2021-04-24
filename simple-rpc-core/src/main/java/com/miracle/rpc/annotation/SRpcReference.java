package com.miracle.rpc.annotation;

import java.lang.annotation.*;

/**
 * 服务发现
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 9:33
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface SRpcReference {

    /**
     * 服务接口:匹配从注册中心获取到本地的服务提供者，得到服务提供者列表，再根据负载均衡策略选取一个发起服务调用
     */
    Class<?> targetItf();

    /**
     * 超时时间：服务调用超时时间
     */
    long timeout() default 3000L;

    /**
     * 负载均衡策略 默认3：轮询
     */
    String loadBalanceStrategy() default "3";

    /**
     * 服务提供者唯一标识
     */
    String remoteAppKey();

    /**
     * 调用者线程数
     */
    int consumeThreads() default 10;

    /**
     * 服务分组组名
     */
    String groupName() default "default";

}
