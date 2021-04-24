package com.miracle.rpc.loadbalance;

import com.miracle.rpc.model.Provider;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载
 * @author miracle
 * @date 2021/4/19 9:57
 */
public class PollingLoadBalanceStrategyImpl implements LoadBalanceStrategy {

    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public Provider select(List<Provider> providers) {

        if (index.get() >= providers.size()) {
            index.set(0);
        }
        Provider provider = providers.get(index.getAndIncrement());
        if (null == provider) {
            return providers.get(0);
        }
        return provider;
    }
}