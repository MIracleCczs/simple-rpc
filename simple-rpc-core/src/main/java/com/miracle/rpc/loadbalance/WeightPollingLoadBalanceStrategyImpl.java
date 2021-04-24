package com.miracle.rpc.loadbalance;

import com.miracle.rpc.model.Provider;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 带权重的轮询
 * @author miracle
 * @date 2021/4/19 10:04
 */
public class WeightPollingLoadBalanceStrategyImpl implements LoadBalanceStrategy {

    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public Provider select(List<Provider> providers) {
        for (Provider provider : providers) {
            int weight = provider.getWeight();
            for (int i = 0; i < weight; i++) {
                providers.add(Provider.copy(provider));
            }
        }
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