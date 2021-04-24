package com.miracle.rpc.util;

import com.google.common.collect.Maps;
import com.miracle.rpc.exception.SRpcException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 9:41
 */
@Slf4j
public class SimpleRpcPropertiesUtil {

    private static final Map<String, Object> PROPERTIES_MAP;

    static {
        PROPERTIES_MAP = Maps.newConcurrentMap();
        try {
            Properties properties = PropertiesLoaderUtils.loadAllProperties("simple-rpc.properties");
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                Object key = entry.getKey();
                PROPERTIES_MAP.put(key.toString(), entry.getValue());
            }
        } catch (IOException e) {
            log.error("SimpleRpcPropertiesUtil load properties error", e);
            throw new SRpcException("SimpleRpcPropertiesUtil load properties error");
        }
    }


    /**
     * 服务提供者唯一标识:作为zookeeper 的子路径（改应用所有服务的命名空间）
     * @return
     */
    public static String getAppKey() {
        return MapUtils.getString(PROPERTIES_MAP, "appKey");
    }

    /**
     * 服务端口：对外发布服务作为netty服务端端口
     * @return
     */
    public static int getServerPort() {
        return MapUtils.getInteger(PROPERTIES_MAP, "serverPort");
    }

    /**
     * zk服务列表
     * @return
     */
    public static String getZkServers() {
        return MapUtils.getString(PROPERTIES_MAP, "zkServers");
    }

    public static int getSessionTimeout() {
        return MapUtils.getInteger(PROPERTIES_MAP, "sessionTimeout");
    }

    public static int getConnectionTimeout() {
        return MapUtils.getInteger(PROPERTIES_MAP, "connectionTimeout");
    }

    /**
     * 序列化方式
     * @return
     */
    public static String getSerializeType() {
        return MapUtils.getString(PROPERTIES_MAP, "serializeType");
    }

    /**
     * 阻塞队列长度
     * @return
     */
    public static int getChannelConnectSize() {
        return MapUtils.getInteger(PROPERTIES_MAP, "channelConnectSize");
    }

    private SimpleRpcPropertiesUtil() {
    }
}
