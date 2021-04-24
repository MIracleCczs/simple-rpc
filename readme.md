### xml方式 spring-rpc-test中
> server (com.miracle.rpc.test.spring.PublishTest)
```xml
    <simple-rpc:service id="userServiceRpc"
                        interface="com.miracle.rpc.test.service.UserService"
                        ref="userService"/>

<bean id="userService" class="com.miracle.rpc.test.service.impl.UserServiceImpl"/>

```
> client (com.miracle.rpc.test.spring.ReferenceTest)

```xml
    <simple-rpc:reference id="remoteUserService"
                          remoteAppKey="test"
                          interface="com.miracle.rpc.test.service.UserService"/>
```

### 注解方式

> server

```java
@SRpcService(serviceItf = UserService.class, workThreads = 8)
```

> client
```java
@SRpcReference(targetItf = UserService.class, remoteAppKey = "test")
```
### 开启注解
```java
@EnableSRpc
```
