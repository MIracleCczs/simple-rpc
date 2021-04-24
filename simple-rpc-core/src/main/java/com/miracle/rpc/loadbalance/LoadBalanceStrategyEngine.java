package com.miracle.rpc.loadbalance;

import com.google.common.collect.Maps;
import com.miracle.rpc.exception.SRpcException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 负载均衡策略引擎
 * @author miracle
 * @date 2021/4/19 10:09
 */
@Slf4j
public class LoadBalanceStrategyEngine {

    private static final Map<LoadBalanceStrategyEnum, LoadBalanceStrategy> LOAD_BALANCE_STRATEGY_MAP = Maps.newConcurrentMap();

    static {
        LOAD_BALANCE_STRATEGY_MAP.put(LoadBalanceStrategyEnum.RANDOM, new RandomLoadBalanceStrategyImpl());
        LOAD_BALANCE_STRATEGY_MAP.put(LoadBalanceStrategyEnum.WEIGHT_RANDOM, new WeightRandomLoadBalanceStrategyImpl());
        LOAD_BALANCE_STRATEGY_MAP.put(LoadBalanceStrategyEnum.POLLING, new PollingLoadBalanceStrategyImpl());
        LOAD_BALANCE_STRATEGY_MAP.put(LoadBalanceStrategyEnum.WEIGHT_POLLING, new WeightPollingLoadBalanceStrategyImpl());
        LOAD_BALANCE_STRATEGY_MAP.put(LoadBalanceStrategyEnum.HASH, new HashLoadBalanceStrategyImpl());
    }

    /**
     * 根据配置加载策略
     * @param loadBalanceStrategy
     * @return
     */
    public static LoadBalanceStrategy getLoadBalanceStrategy(String loadBalanceStrategy) {
        LoadBalanceStrategyEnum strategyEnum = LoadBalanceStrategyEnum.getByCode(loadBalanceStrategy);
        if (strategyEnum == null) {
            log.debug("can't find strategy with property loadBalanceStrategy = {}", loadBalanceStrategy);
            throw new SRpcException("can't find strategy with property loadBalanceStrategy：" + loadBalanceStrategy);
        } else {
            return LOAD_BALANCE_STRATEGY_MAP.get(strategyEnum);
        }
    }
}