package com.miracle.rpc.spring;

import com.miracle.rpc.annotation.SRpcService;
import com.miracle.rpc.model.Provider;
import com.miracle.rpc.register.RegisterCenterImpl;
import com.miracle.rpc.transport.NettyServer;
import com.miracle.rpc.util.IpUtil;
import com.miracle.rpc.util.SimpleRpcPropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 9:31
 */
@Slf4j
public class AnnotationServicesPublisher implements ApplicationListener<ApplicationContextEvent>, ApplicationContextAware, DisposableBean {

    private ApplicationContext applicationContext;

    @Override
    public void destroy() {
        /* 销毁 */
        log.debug("AnnotationServicesPublisher bean destroy");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {

        if (event.getApplicationContext() != applicationContext) {
            log.debug("Received a event from another application context {}, ignoring it", event.getApplicationContext());
        } else {
            if (event instanceof ContextRefreshedEvent) {
                // 1. 获取所有注解的Bean
                Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(SRpcService.class);
                if (MapUtils.isEmpty(beansWithAnnotation)) {
                    log.info("no simple rpc exist");
                } else {
                    for (Object bean : beansWithAnnotation.values()) {
                        if (bean.getClass().isAnnotationPresent(SRpcService.class)) {
                            // 2. 解析bean
                            List<Provider> providers = this.buildProviderService(bean);
                            // 3. 启动服务
                            NettyServer.getInstance().startServer(SimpleRpcPropertiesUtil.getServerPort());
                            // 4. 将服务打包注册
                            RegisterCenterImpl.getInstance().registerProvider(providers);
                        }
                    }
                }
            } else if (event instanceof ContextClosedEvent){
                // 销毁所有的服务
                log.debug("Received a context closed event, now close it and destroy all simple rpc service");
                RegisterCenterImpl.getInstance().destroy(null);
            }
        }
    }

    /**
     * 将服务接口按照方法切分，注册到注册中心
     * @return List<Provider>
     */
    private List<Provider> buildProviderService(Object bean) {
        List<Provider> providers = new ArrayList<>();
        SRpcService sRpcService = bean.getClass().getAnnotation(SRpcService.class);
        Class<?> serviceItf = sRpcService.serviceItf();
        String groupName = sRpcService.groupName();
        long timeout = sRpcService.timeout();
        int weight = sRpcService.weight();
        int workThreads = sRpcService.workThreads();

        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            Provider provider = Provider.builder().serviceItf(serviceItf)
                    .serviceObject(bean)
                    .serverPort(SimpleRpcPropertiesUtil.getServerPort())
                    .timeout(timeout)
                    .appKey(SimpleRpcPropertiesUtil.getAppKey())
                    .groupName(groupName)
                    .weight(weight)
                    .workerThreads(workThreads)
                    .serverIp(IpUtil.getLocalIP())
                    .serviceMethod(method).build();
            providers.add(provider);
        }
        return providers;
    }
}
