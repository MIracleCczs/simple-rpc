## simple-rpc
> 基于netty的rpc通信框架

### 完成基础工程搭建以及序列化和反序列化

```java
public interface ISerializer {

    /**
     * 序列化
     * @param obj
     * @param <T>
     * @return
     */
    <T> byte[] serialize(T obj);

    /**
     * 反序列化
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T deserialize(byte[] data, Class<T> clazz);
}
```

提供同一入口，和多种实现，包括`java 默认实现(DefaultSerializer)`，`json(JacksonSerializer&FastJsonSerializer)`，`hessian(HessianSerializer)`，`protobuf(ProtoBufSerializer)`。

还提供了序列化工具引擎`SerializerEngine`选择对应的序列化方式对外提供序列化和反序列服务。

### 分布式服务框架的发布和引入

#### spring

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>5.2.9.RELEASE</version>
</dependency>        
```

> 通过spring对服务发布和引入，交由IOC容器管理，屏蔽本地调用和远程调用的差异性。

#### 发布

##### 方式一：自定义ProviderFactoryBean
> 通过解析自定义schema文件创建
```java
    private static class ProviderFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
        @Override
        protected void doParse(Element element, BeanDefinitionBuilder builder) {
            try {
                String serviceItf = element.getAttribute("interface");
                String timeout = element.getAttribute("timeout");
                String serverPort = element.getAttribute("serverPort");
                String ref = element.getAttribute("ref");
                String weight = element.getAttribute("weight");
                String workerThreads = element.getAttribute("workerThreads");
                String appKey = element.getAttribute("appKey");
                String groupName = element.getAttribute("groupName");

                builder.addPropertyValue("serverPort", Integer.parseInt(serverPort));
                builder.addPropertyValue("timeout", Integer.parseInt(timeout));
                builder.addPropertyValue("serviceItf", Class.forName(serviceItf));
                builder.addPropertyReference("serviceObject", ref);
                builder.addPropertyValue("appKey", appKey);
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
```
```xml
    <xsd:element name="service">
        <xsd:complexType>
            <xsd:complexContent>
                <xsd:extension base="beans:identifiedType">
                    <xsd:attribute name="interface" type="xsd:string" use="required"/>
                    <xsd:attribute name="timeout" type="xsd:int"/>
                    <xsd:attribute name="ref" type="xsd:string" use="required"/>
                    <xsd:attribute name="weight" type="xsd:int"/>
                    <xsd:attribute name="workerThreads" type="xsd:int"/>
                    <xsd:attribute name="groupName" type="xsd:string"/>
                </xsd:extension>
            </xsd:complexContent>
        </xsd:complexType>
    </xsd:element>
```

##### 方式二：自定义注解 SRpcService
```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SRpcService {

    /**
     * 服务接口:用于注册在服务注册中心，服务调用端获取后缓存再本地用于发起服务调用
     */
    Class<?> serviceItf() default void.class;
    /**
     * 超时时间：控制服务端超时时间 ms
     */
    long timeout() default 3000L;
    /**
     * 服务分组组名：可用于分组灰度发布，配置不同分组，可以让调用都路由到配置了相同分组的路由上
     */
    String groupName() default "default";
    /**
     * 服务提供者权重：配置该机器在集群中的权重，用于某些负载均衡算法
     */
    int weight() default 1;
    /**
     * 服务端线程数：限制服务端改服务线程数，服务端限流
     */
    int workThreads() default 10;
}
```
> 通过AnnotationServicesPublisher启动。

#### 引入

##### 方式一：自定义DiscoverFactoryBean
> 通过解析自定义schema文件创建
```java
    private static class DiscoverFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

        @Override
        protected Class<?> getBeanClass(Element element) {
            return DiscoverFactoryBean.class;
        }

        @Override
        protected void doParse(Element element, BeanDefinitionBuilder builder) {
            try {
                String targetInterface = element.getAttribute("interface");
                String timeout = element.getAttribute("timeout");
                String loadBalanceStrategy = element.getAttribute("loadBalanceStrategy");
                String remoteAppKey = element.getAttribute("remoteAppKey");
                String groupName = element.getAttribute("groupName");
                String consumeThreads = element.getAttribute("consumeThreads");

                builder.addPropertyValue("targetItf", Class.forName(targetInterface));
                builder.addPropertyValue("timeout", Long.valueOf(timeout));
                if (StringUtils.isNotBlank(loadBalanceStrategy)) {
                    builder.addPropertyValue("loadBalanceStrategy", loadBalanceStrategy);
                }
                builder.addPropertyValue("remoteAppKey", remoteAppKey);
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
```
```xml
    <xsd:element name="reference">
        <xsd:complexType>
            <xsd:complexContent>
                <xsd:extension base="beans:identifiedType">
                    <xsd:attribute name="interface" type="xsd:string" use="required"/>
                    <xsd:attribute name="timeout" type="xsd:int"/>
                    <xsd:attribute name="consumeThreads" type="xsd:int"/>
                    <xsd:attribute name="loadBalanceStrategy" type="xsd:string"/>
                    <xsd:attribute name="remoteAppKey" type="xsd:string" use="required"/>
                    <xsd:attribute name="groupName" type="xsd:string"/>
                </xsd:extension>
            </xsd:complexContent>
        </xsd:complexType>
    </xsd:element>
```

##### 方式二：自定义注解
```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface SRpcReference {

    /**
     * 服务接口:匹配从注册中心获取到本地的服务提供者，得到服务提供者列表，再根据负载均衡策略选取一个发起服务调用
     */
    Class<?> targetItf();

    /**
     * 超时时间：服务调用超时时间
     */
    long timeout() default 3000L;

    /**
     * 负载均衡策略 默认3：轮询
     */
    String loadBalanceStrategy() default "3";

    /**
     * 服务提供者唯一标识
     */
    String remoteAppKey();

    /**
     * 调用者线程数
     */
    int consumeThreads() default 10;

    /**
     * 服务分组组名
     */
    String groupName() default "default";

}
```
> 通过ReferenceBeanPostProcessor发现服务注册bean。

### 分布式服务框架注册中心
1. 在服务启动时，将服务提供者信息主动上报到服务注册中心
2. 服务调用者启动时，将服务提供者信息从注册中心下拉到服务调用者本地缓存，服务调用者从本地缓存的服务提供者地址列表中，基于某种负载均衡策略发起服务调用。
3. 服务注册中心能够感知服务提供者集群中某台机器下线，将该机器服务提供者信息从注册中心中删除，并通知所有服务调用方。
### Zookeeper
> 服务启动时，将服务提供者信息拼接路径创建Zookeeper临时节点，
> 服务消费端在发起服务调用前，先连接到Zookeeper，对服务提供者路径注册监听器，同时获取提供者信息到本地缓存。

#### 注册服务到Zookeeper
```java
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
```

### 服务启动

NettyServer

### Channel复用

NettyChannelPoolFactory

### 服务调用方动态代理

RevokerProxyBeanFactory

### 负载均衡策略

```java

public interface LoadBalanceStrategy {

    /**
     * 根据负载均衡算法从服务提供者列表中获取
     * @param providers
     * @return
     */
    Provider select(List<Provider> providers);
}
```

```java

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
}

```