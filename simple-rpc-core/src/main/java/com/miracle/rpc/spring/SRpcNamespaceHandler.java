package com.miracle.rpc.spring;

import com.miracle.rpc.exception.SRpcException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.w3c.dom.Element;

/**
 * simple-rpc.xsd 解析器
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 9:27
 */
@Slf4j
public class SRpcNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("service", new ProviderFactoryBeanDefinitionParser());
        registerBeanDefinitionParser("reference", new DiscoverFactoryBeanDefinitionParser());
    }


    private static class ProviderFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

        @Override
        protected Class<?> getBeanClass(Element element) {
            return ProviderFactoryBean.class;
        }

        @Override
        protected void doParse(Element element, BeanDefinitionBuilder builder) {
            try {
                // 服务提供方接口
                String serviceItf = element.getAttribute("interface");
                // 服务方超时时间
                String timeout = element.getAttribute("timeout");
                // 接口实现
                String ref = element.getAttribute("ref");
                // 权重
                String weight = element.getAttribute("weight");
                String workerThreads = element.getAttribute("workerThreads");
                String groupName = element.getAttribute("groupName");

                /* require */
                builder.addPropertyValue("serviceItf", Class.forName(serviceItf));
                builder.addPropertyReference("serviceObject", ref);

                if (NumberUtils.isCreatable(timeout)) {
                    builder.addPropertyValue("timeout", Long.valueOf(timeout));
                }
                if (NumberUtils.isCreatable(weight)) {
                    builder.addPropertyValue("weight", Integer.parseInt(weight));
                }
                if (NumberUtils.isCreatable(workerThreads)) {
                    builder.addPropertyValue("workerThreads", Integer.parseInt(workerThreads));
                }
                if (StringUtils.isNotBlank(groupName)) {
                    builder.addPropertyValue("groupName", groupName);
                }
            } catch (Exception e) {
                log.error("SRpcNamespaceHandler.ProviderFactoryBeanDefinitionParser doParse error ", e);
                throw new SRpcException("SRpcNamespaceHandler.ProviderFactoryBeanDefinitionParser doParse error ", e);
            }
        }
    }

    private static class DiscoverFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

        @Override
        protected Class<?> getBeanClass(Element element) {
            return DiscoverFactoryBean.class;
        }

        @Override
        protected void doParse(Element element, BeanDefinitionBuilder builder) {
            try {
                // 目标远程服务接口
                String targetInterface = element.getAttribute("interface");
                // 调用方超时时间
                String timeout = element.getAttribute("timeout");
                // 负载均衡策略
                String loadBalanceStrategy = element.getAttribute("loadBalanceStrategy");
                // 服务提供方唯一标识
                String remoteAppKey = element.getAttribute("remoteAppKey");
                // 服务组名
                String groupName = element.getAttribute("groupName");
                // 消费组线程数
                String consumeThreads = element.getAttribute("consumeThreads");

                // require
                builder.addPropertyValue("targetItf", Class.forName(targetInterface));
                builder.addPropertyValue("remoteAppKey", remoteAppKey);

                if (NumberUtils.isCreatable(timeout)) {
                    builder.addPropertyValue("timeout", Long.valueOf(timeout));
                }
                if (StringUtils.isNotBlank(loadBalanceStrategy)) {
                    builder.addPropertyValue("loadBalanceStrategy", loadBalanceStrategy);
                }
                if (StringUtils.isNotBlank(groupName)) {
                    builder.addPropertyValue("groupName", groupName);
                }
                if (StringUtils.isNotBlank(consumeThreads)) {
                    builder.addPropertyValue("consumeThreads", consumeThreads);
                }
            } catch (Exception e) {
                log.error("SRpcNamespaceHandler.DiscoverFactoryBeanDefinitionParser doParse error ", e);
                throw new SRpcException("SRpcNamespaceHandler.DiscoverFactoryBeanDefinitionParser doParse error ", e);
            }
        }
    }
}
