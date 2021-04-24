package com.miracle.rpc.loadbalance;

import com.miracle.rpc.model.Provider;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

/**
 * 随机负载
 * @author miracle
 * @date 2021/4/19 9:46
 */
public class RandomLoadBalanceStrategyImpl implements LoadBalanceStrategy {

    @Override
    public Provider select(List<Provider> providers) {
        int maxLen = providers.size();
        int random = RandomUtils.nextInt(0, maxLen - 1);
        return providers.get(random);
    }
}