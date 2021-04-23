## simple-rpc
> 基于netty的rpc通信框架

### day1:完成基础工程搭建以及序列化和反序列化

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