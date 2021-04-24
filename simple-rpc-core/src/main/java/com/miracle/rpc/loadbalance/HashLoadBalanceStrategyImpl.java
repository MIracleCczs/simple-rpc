package com.miracle.rpc.loadbalance;

import com.miracle.rpc.model.Provider;
import com.miracle.rpc.util.IpUtil;

import java.util.List;

/**
 * 源地址hash负载
 *
 * @author miracle
 * @date 2021/4/19 10:07
 */
public class HashLoadBalanceStrategyImpl implements LoadBalanceStrategy {

    @Override
    public Provider select(List<Provider> providers) {
        int hashCode = IpUtil.getLocalIP().hashCode();
        return providers.get(providers.size() % hashCode);
    }
}