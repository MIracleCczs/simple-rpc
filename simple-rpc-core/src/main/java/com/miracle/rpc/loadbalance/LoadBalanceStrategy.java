package com.miracle.rpc.loadbalance;

import com.miracle.rpc.model.Provider;

import java.util.List;

/**
 * 负载均衡策略
 * @author miracle
 * @date 2021/4/19 9:44
 */
public interface LoadBalanceStrategy {

    /**
     * 根据负载均衡算法从服务提供者列表中获取
     * @param providers
     * @return
     */
    Provider select(List<Provider> providers);
}
