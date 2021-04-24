package com.miracle.rpc.spring;

import com.miracle.rpc.model.Invoker;
import com.miracle.rpc.model.Provider;
import com.miracle.rpc.register.IRegisterCenter;
import com.miracle.rpc.register.RegisterCenterImpl;
import com.miracle.rpc.transport.NettyChannelPoolFactory;
import com.miracle.rpc.transport.RevokerProxyBeanFactory;
import lombok.Data;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;

/**
 * 服务发现
 *      1.通过注册中心，将服务提供者获取到本地缓存列表
 *      2.初始化Netty连接池
 *      3.获取服务提供者代理对象
 *      4.将服务消费者信息注册到注册中心
 *
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 9:28
 */
@Data
public class DiscoverFactoryBean implements FactoryBean, InitializingBean {
    /**
     * 服务接口:匹配从注册中心获取到本地的服务提供者，得到服务提供者列表，再根据负载均衡策略选取一个发起服务调用
     */
    private Class<?> targetItf;

    /**
     * 超时时间：服务调用超时时间
     */
    private Long timeout = 3000L;

    /**
     * 服务bean：远程服务生成的本地代理对象
     */
    private Object serviceObject;

    /**
     * 负载均衡策略 默认3：轮询
     */
    private String loadBalanceStrategy = "3";

    /**
     * 服务提供者唯一标识
     */
    private String remoteAppKey;

    /**
     * 调用者线程数
     */
    private Integer consumeThreads = 10;

    /**
     * 服务分组组名
     */
    private String groupName = "default";

    @Override
    public Object getObject() throws Exception {
        return serviceObject;
    }

    @Override
    public Class<?> getObjectType() {
        return targetItf;
    }

    @Override
    public void afterPropertiesSet() throws Exception {


        // 1.通过注册中心，将服务提供者获取到本地缓存列表
        IRegisterCenter registerCenter = RegisterCenterImpl.getInstance();
        registerCenter.initProviderMap(remoteAppKey, groupName);
        Map<String, List<Provider>> serviceMetadata = registerCenter.getServiceMetadata();
        // 2.初始化Netty连接池
        NettyChannelPoolFactory.getInstance().initChannelPoolFactory(serviceMetadata);
        // 3.获取服务提供者代理对象
        RevokerProxyBeanFactory revokerProxyBeanFactory = RevokerProxyBeanFactory.getInstance(targetItf, timeout, consumeThreads, loadBalanceStrategy);
        this.serviceObject = revokerProxyBeanFactory.getProxy();
        // 4.将服务消费者信息注册到注册中心
        Invoker invoker = Invoker.builder().groupName(groupName)
                .remoteAppKey(remoteAppKey)
                .targetItf(targetItf)
                .build();
        registerCenter.registerInvoker(invoker);

    }
}
