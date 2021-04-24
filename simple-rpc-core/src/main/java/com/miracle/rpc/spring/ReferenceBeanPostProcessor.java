package com.miracle.rpc.spring;

import com.miracle.rpc.annotation.SRpcReference;
import com.miracle.rpc.exception.SRpcException;
import com.miracle.rpc.model.Invoker;
import com.miracle.rpc.model.Provider;
import com.miracle.rpc.register.IRegisterCenter;
import com.miracle.rpc.register.RegisterCenterImpl;
import com.miracle.rpc.transport.NettyChannelPoolFactory;
import com.miracle.rpc.transport.RevokerProxyBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 9:35
 */
@Slf4j
public class ReferenceBeanPostProcessor implements InstantiationAwareBeanPostProcessor {


    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        return true;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            SRpcReference reference = field.getAnnotation(SRpcReference.class);
            if (null != reference) {
                // 1.通过注册中心，将服务提供者获取到本地缓存列表
                IRegisterCenter registerCenter = RegisterCenterImpl.getInstance();
                registerCenter.initProviderMap(reference.remoteAppKey(), reference.groupName());
                Map<String, List<Provider>> serviceMetadata = registerCenter.getServiceMetadata();
                // 2.初始化Netty连接池
                NettyChannelPoolFactory.getInstance().initChannelPoolFactory(serviceMetadata);
                // 3.获取服务提供者代理对象
                Object proxy = RevokerProxyBeanFactory.getInstance(reference.targetItf(), reference.timeout(),
                        reference.consumeThreads(), reference.loadBalanceStrategy()).getProxy();
                // 4.将服务消费者信息注册到注册中心
                Invoker invoker = Invoker.builder().groupName(reference.groupName())
                        .remoteAppKey(reference.remoteAppKey())
                        .targetItf(reference.targetItf())
                        .build();
                registerCenter.registerInvoker(invoker);

                ReflectionUtils.makeAccessible(field);

                try {
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    log.error("ReferenceBeanPostProcessor post process properties error, beanName={}", beanName, e);
                    throw new SRpcException("ReferenceBeanPostProcessor post process properties error, beanName=" + beanName, e);
                }
            }
        }
        return pvs;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
