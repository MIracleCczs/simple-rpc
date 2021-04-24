package com.miracle.rpc.annotation;

import java.lang.annotation.*;

/**
 * 服务发布
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 9:33
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SRpcService {

    /**
     * 服务接口:用于注册在服务注册中心，服务调用端获取后缓存再本地用于发起服务调用
     */
    Class<?> serviceItf();
    /**
     * 超时时间：控制服务端超时时间 ms
     */
    long timeout() default 3000L;
    /**
     * 服务分组组名：可用于分组灰度发布，配置不同分组，可以让调用都路由到配置了相同分组的路由上
     */
    String groupName() default "default";
    /**
     * 服务提供者权重：配置该机器在集群中的权重，用于某些负载均衡算法
     */
    int weight() default 1;
    /**
     * 服务端线程数：限制服务端改服务线程数，服务端限流
     */
    int workThreads() default 10;
}

