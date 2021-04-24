package com.miracle.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 服务提供者信息
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 9:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Provider implements Serializable {
    private Class<?> serviceItf;
    private transient Object serviceObject;
    private Integer serverPort;
    private Long timeout;
    private transient Object serviceProxyObject;
    private String appKey;
    private String groupName = "default";
    private Integer weight = 1;
    private Integer workerThreads = 10;
    private String serverIp;
    private transient Method serviceMethod;

    /**
     * 拷贝
     * @param provider 服务提供者
     * @return
     */
    public static Provider copy(Provider provider) {
        return Provider.builder().serviceItf(provider.getServiceItf())
                .serviceObject(provider.getServiceObject())
                .serverPort(provider.getServerPort())
                .timeout(provider.getTimeout())
                .serviceProxyObject(provider.getServiceProxyObject())
                .appKey(provider.getAppKey())
                .groupName(provider.getGroupName())
                .weight(provider.getWeight())
                .workerThreads(provider.getWorkerThreads())
                .serverIp(provider.getServerIp())
                .serviceMethod(provider.getServiceMethod())
                .build();
    }
}
