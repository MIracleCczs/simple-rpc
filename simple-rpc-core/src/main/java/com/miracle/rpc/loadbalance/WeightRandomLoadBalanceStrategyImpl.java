package com.miracle.rpc.loadbalance;

import com.miracle.rpc.model.Provider;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

/**
 * 带权重的随机负载
 * @author miracle
 * @date 2021/4/19 9:49
 */
public class WeightRandomLoadBalanceStrategyImpl implements LoadBalanceStrategy {

    @Override
    public Provider select(List<Provider> providers) {
        for (Provider provider : providers) {
            int weight = provider.getWeight();
            for (int i = 0; i < weight; i++) {
                providers.add(Provider.copy(provider));
            }
        }
        int maxLen = providers.size();
        int random = RandomUtils.nextInt(0, maxLen - 1);
        return providers.get(random);
    }
}