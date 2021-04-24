package com.miracle.rpc.register;

import com.miracle.rpc.model.Invoker;
import com.miracle.rpc.model.Provider;

import java.util.List;
import java.util.Map;

/**
 * 服务注册中心
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 9:36
 */
public interface IRegisterCenter {

    /**
     * 注册服务提供者信息
     * @param providers 服务提供者
     */
    void registerProvider(List<Provider> providers);

    /**
     * 获取服务提供者列表
     * key:接口
     * @return
     */
    Map<String, List<Provider>> getProvidersMap();

    /**
     * 销毁
     * @param serviceItfName 接口名称
     */
    void destroy(String serviceItfName);

    /**
     * 消费端初始化本地服务缓存
     * @param remoteAppKey 服务提供者唯一标识
     * @param groupName 服务组名
     */
    void initProviderMap(String remoteAppKey, String groupName);

    /**
     * 获取服务提供者信息
     * @return
     */
    Map<String, List<Provider>> getServiceMetadata();

    /**
     * 注册服务消费者信息 用于监控
     * @param invoker
     */
    void registerInvoker(Invoker invoker);
}
