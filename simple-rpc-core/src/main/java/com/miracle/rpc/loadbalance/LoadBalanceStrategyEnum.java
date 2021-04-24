package com.miracle.rpc.loadbalance;

import org.apache.commons.lang3.StringUtils;

/**
 * 负载均衡策略
 *
 * @author miracle
 * @date 2021/4/19 10:09
 */
public enum LoadBalanceStrategyEnum {

    /**
     * 随机
     */
    RANDOM("1"),
    /**
     * 带权重随机
     */
    WEIGHT_RANDOM("2"),
    /**
     * 轮询
     */
    POLLING("3"),
    /**
     * 带权重轮询
     */
    WEIGHT_POLLING("4"),
    /**
     * 哈希
     */
    HASH("5");

    private String code;

    LoadBalanceStrategyEnum(String code) {
        this.code = code;
    }

    public static LoadBalanceStrategyEnum getByCode(String code) {
        LoadBalanceStrategyEnum[] strategyEnums = LoadBalanceStrategyEnum.values();
        for (LoadBalanceStrategyEnum strategyEnum : strategyEnums) {
            if (StringUtils.equals(strategyEnum.getCode(), code)) {
                return strategyEnum;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }
}
