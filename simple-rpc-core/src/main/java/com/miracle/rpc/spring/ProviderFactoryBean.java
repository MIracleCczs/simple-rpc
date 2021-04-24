package com.miracle.rpc.spring;

import com.miracle.rpc.model.Provider;
import com.miracle.rpc.register.RegisterCenterImpl;
import com.miracle.rpc.transport.NettyServer;
import com.miracle.rpc.util.IpUtil;
import com.miracle.rpc.util.SimpleRpcPropertiesUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义服务发布者 factory bean
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 9:24
 */
@Data
@Slf4j
public class ProviderFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

    /**
     * 服务接口:用于注册在服务注册中心，服务调用端获取后缓存再本地用于发起服务调用
     */
    private Class<?> serviceItf;

    /**
     * 服务实现类：服务调用
     */
    private Object serviceObject;
    /**
     * 超时时间：控制服务端超时时间 ms
     */
    private Long timeout;

    /**
     * 服务代理对象
     */
    private Object serviceProxyObject;

    /**
     * 服务分组组名：可用于分组灰度发布，配置不同分组，可以让调用都路由到配置了相同分组的路由上
     */
    private String groupName = "default";

    /**
     * 服务提供者权重：配置该机器在集群中的权重，用于某些负载均衡算法
     */
    private Integer weight = 1;

    /**
     * 服务端线程数：限制服务端改服务线程数，服务端限流
     */
    private Integer workerThreads = 10;


    @Override
    public Object getObject() {
        return serviceProxyObject;
    }

    @Override
    public Class<?> getObjectType() {
        return serviceItf;
    }

    @Override
    public void afterPropertiesSet() {
        // 1.启动服务
        NettyServer.getInstance().startServer(SimpleRpcPropertiesUtil.getServerPort());
        // 2. 将服务打包注册到注册中心
        List<Provider> providers = buildProviderService();
        RegisterCenterImpl.getInstance().registerProvider(providers);
    }

    @Override
    public void destroy() {
        log.debug("ProviderFactoryBean for {} destroy", serviceItf.getName());
        RegisterCenterImpl.getInstance().destroy(serviceItf.getName());
    }

    /**
     * 将服务接口按照方法切分，注册到注册中心
     * @return List<Provider>
     */
    private List<Provider> buildProviderService() {
        List<Provider> providers = new ArrayList<>();
        Method[] methods = serviceObject.getClass().getMethods();
        for (Method method : methods) {
            Provider provider = Provider.builder().serviceItf(serviceItf)
                    .serviceObject(serviceObject)
                    .serviceProxyObject(serviceProxyObject)
                    .serverPort(SimpleRpcPropertiesUtil.getServerPort())
                    .timeout(timeout)
                    .appKey(SimpleRpcPropertiesUtil.getAppKey())
                    .groupName(groupName)
                    .weight(weight)
                    .workerThreads(workerThreads)
                    .serverIp(IpUtil.getLocalIP())
                    .serviceMethod(method).build();
            providers.add(provider);
        }
        return providers;
    }
}
