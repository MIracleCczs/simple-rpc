package com.miracle.rpc.register;

import com.google.common.collect.Maps;
import com.miracle.rpc.exception.SRpcException;
import com.miracle.rpc.model.Invoker;
import com.miracle.rpc.model.Provider;
import com.miracle.rpc.util.IpUtil;
import com.miracle.rpc.util.SimpleRpcPropertiesUtil;
import com.miracle.rpc.util.UrlConstants;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务注册
 * zk节点：根目录/APP_KEY/服务组名/服务接口类路径/ip|端口|权重|线程数|组名
 *
 * 1.注册服务提供者
 * 2.本地服务缓存
 * 3.注册服务调用者信息
 * 4.监听节点变化，刷新服务提供者信息
 *
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 9:38
 */
@Slf4j
public class RegisterCenterImpl implements IRegisterCenter{

    private static final RegisterCenterImpl REGISTER_CENTER = new RegisterCenterImpl();

    private volatile ZkClient zkClient = null;

    private static final String ZK_SERVERS = SimpleRpcPropertiesUtil.getZkServers();
    private static final int ZK_SESSION_TIMEOUT = SimpleRpcPropertiesUtil.getSessionTimeout();
    private static final int ZK_CONNECT_TIMEOUT = SimpleRpcPropertiesUtil.getConnectionTimeout();

    private static final String ROOT_PATH = "/config_register/" + SimpleRpcPropertiesUtil.getAppKey();
    public static final String PROVIDER_TYPE = "/provider";
    public static final String INVOKER_TYPE = "/invoker";

    private static final int SERVER_PORT = SimpleRpcPropertiesUtil.getServerPort();
    private static final String LOCAL_IP = IpUtil.getLocalIP();

    private static final Map<String, List<Provider>> PROVIDER_MAP = Maps.newConcurrentMap();

    private static final Map<String, List<Provider>> SERVICE_METADATA = Maps.newConcurrentMap();

    public static RegisterCenterImpl getInstance() {
        return REGISTER_CENTER;
    }

    @Override
    public void registerProvider(List<Provider> providers) {
        if (CollectionUtils.isEmpty(providers)) {
            log.debug("RegisterCenterImpl registerProvider providers is empty, ignore it, providers={}", providers);
        } else {
            synchronized (RegisterCenterImpl.class) {
                this.lazyInitZkClient();

                this.setLocalCache(providers);

                String rootNode = ROOT_PATH + UrlConstants.SLASH + providers.get(0).getAppKey();
                this.createRootNode(rootNode);

                for (Map.Entry<String, List<Provider>> entry : PROVIDER_MAP.entrySet()) {
                    // 服务接口类路径
                    String serviceItfName = entry.getKey();
                    Provider provider = entry.getValue().get(0);
                    // 服务组名
                    String groupName = entry.getValue().get(0).getGroupName();
                    // 创建服务提供者节点
                    String servicePath = rootNode + UrlConstants.SLASH + groupName + UrlConstants.SLASH + serviceItfName + PROVIDER_TYPE;
                    this.createServiceNode(servicePath);

                    // 创建当前服务器临时节点
                    String currentServiceIpNode = servicePath + UrlConstants.SLASH + LOCAL_IP
                            + UrlConstants.VERTICAL_LINE
                            + SERVER_PORT
                            + UrlConstants.VERTICAL_LINE
                            + provider.getWeight()
                            + UrlConstants.VERTICAL_LINE
                            + provider.getWorkerThreads()
                            + UrlConstants.VERTICAL_LINE
                            + groupName;

                    this.createCurrentServiceIpNode(currentServiceIpNode);
                    log.debug("create current service node success, node path = {}", currentServiceIpNode);

                    // 监听本地服务变化，加入本地缓存
                    subscribeChildChanges(servicePath, PROVIDER_MAP);
                }
            }
        }

    }

    @Override
    public Map<String, List<Provider>> getProvidersMap() {
        return PROVIDER_MAP;
    }

    @Override
    public void destroy(String serviceItfName) {
        if (StringUtils.isNotEmpty(serviceItfName)) {
            PROVIDER_MAP.remove(serviceItfName);
        } else {
            PROVIDER_MAP.clear();
        }
    }

    @Override
    public void initProviderMap(String remoteAppKey, String groupName) {
        if (MapUtils.isEmpty(SERVICE_METADATA)) {
            SERVICE_METADATA.putAll(fetchOrUpdateServiceMetaData(remoteAppKey, groupName));
        }
    }

    @Override
    public void registerInvoker(Invoker invoker) {
        if (invoker == null) {
            log.error("invoker can't be null");
            throw new SRpcException("invoker can't be null");
        } else {
            synchronized (RegisterCenterImpl.class) {
                this.lazyInitZkClient();

                String rootNode = ROOT_PATH + UrlConstants.SLASH + invoker.getRemoteAppKey();
                this.createRootNode(rootNode);

                String targetItfName = invoker.getTargetItf().getName();
                String servicePath = rootNode + UrlConstants.SLASH + invoker.getGroupName()
                        + UrlConstants.SLASH + targetItfName + INVOKER_TYPE;

                this.createServiceNode(servicePath);
                String currentServiceIpNode = servicePath + UrlConstants.SLASH + LOCAL_IP;
                this.createCurrentServiceIpNode(currentServiceIpNode);
            }
        }
    }

    @Override
    public Map<String, List<Provider>> getServiceMetadata() {
        return SERVICE_METADATA;
    }

    /**
     * 懒加载Zk 客户端
     */
    private void lazyInitZkClient() {
        if (null == zkClient) {
            zkClient = new ZkClient(ZK_SERVERS, ZK_SESSION_TIMEOUT, ZK_CONNECT_TIMEOUT, new SerializableSerializer());
        }
    }

    /**
     * 设置本地缓存
     *
     * @param providers
     */
    private void setLocalCache(List<Provider> providers) {
        for (Provider provider : providers) {
            // 接口类名
            String serviceItfName = provider.getServiceItf().getName();
            List<Provider> loadedProviders = PROVIDER_MAP.get(serviceItfName);
            if (null == loadedProviders) {
                loadedProviders = new ArrayList<>();
            }
            loadedProviders.add(provider);
            PROVIDER_MAP.put(serviceItfName, loadedProviders);
        }
    }

    /**
     * 创建根节点
     *
     * @param rootNode
     */
    private void createRootNode(String rootNode) {
        // 创建zk命名空间
        if (!zkClient.exists(rootNode)) {
            zkClient.createPersistent(rootNode, true);
        }
        // 创建服务提供者节点
        if (!zkClient.exists((rootNode))) {
            zkClient.createPersistent(rootNode);
        }
    }

    /**
     * 创建服务节点
     *
     * @param servicePath
     */
    private void createServiceNode(String servicePath) {
        // 创建服务提供者节点
        if (!zkClient.exists((servicePath))) {
            zkClient.createPersistent(servicePath, true);
        }
    }

    /**
     * 创建当前服务器节点
     *
     * @param currentServiceIpNode
     */
    private void createCurrentServiceIpNode(String currentServiceIpNode) {
        if (!zkClient.exists(currentServiceIpNode)) {
            // 临时节点
            zkClient.createEphemeral(currentServiceIpNode);
        }
    }

    /**
     * 注册监听
     * @param servicePath
     * @param dataMap
     */
    private void subscribeChildChanges(String servicePath, Map<String, List<Provider>> dataMap) {
        zkClient.subscribeChildChanges(servicePath, (parentPath, currentChildList) -> {
            if (currentChildList == null) {
                currentChildList = new ArrayList<>();
            }
            // 获取存活的Ip
            List<String> serviceIpList = currentChildList.stream().map(s -> {
                String[] serverAddress = StringUtils.split(s, UrlConstants.VERTICAL_LINE);
                if (ArrayUtils.isNotEmpty(serverAddress)) {
                    return serverAddress[0];
                }
                return null;
            }).collect(Collectors.toList());
            // 刷新数据
            this.refreshServiceMap(serviceIpList, dataMap);
        });
    }

    /**
     * 刷新服务，将下线服务移除
     *
     * @param activeServiceIpList 存活IP
     */
    private void refreshServiceMap(List<String> activeServiceIpList, Map<String, List<Provider>> dataMap) {
        Map<String, List<Provider>> newestCurrentServiceMap = this.getNewestCurrentServiceMap(activeServiceIpList, dataMap);
        dataMap.clear();
        dataMap.putAll(newestCurrentServiceMap);
    }

    /**
     * 获取本地最新的服务列表
     *
     * @param serviceIpList
     * @param dataMap
     * @return
     */
    private Map<String, List<Provider>> getNewestCurrentServiceMap(List<String> serviceIpList, Map<String, List<Provider>> dataMap) {
        Map<String, List<Provider>> currentServiceMetaMap = Maps.newHashMap();
        for (Map.Entry<String, List<Provider>> entry : dataMap.entrySet()) {
            String serviceItfName = entry.getKey();
            List<Provider> providers = entry.getValue();
            List<Provider> providerList = currentServiceMetaMap.get(serviceItfName);
            if (null == providerList) {
                providerList = new ArrayList<>();
            }
            for (Provider provider : providers) {
                if (serviceIpList.contains(provider.getServerIp())) {
                    providerList.add(provider);
                }
            }
            currentServiceMetaMap.put(serviceItfName, providerList);
        }
        return currentServiceMetaMap;
    }

    private Map<String, List<Provider>> fetchOrUpdateServiceMetaData(String remoteAppKey, String groupName) {
        final Map<String, List<Provider>> providerServiceMap = Maps.newHashMap();
        // 连接ZK
        this.lazyInitZkClient();

        // 服务提供者节点
        String providerNode = ROOT_PATH + UrlConstants.SLASH + remoteAppKey + UrlConstants.SLASH + groupName;
        // 从ZK获取方服务提供者列表
        List<String> providerServices = zkClient.getChildren(providerNode);
        for (String serviceName : providerServices) {
            String servicePath = providerNode + UrlConstants.SLASH + serviceName + PROVIDER_TYPE;
            List<String> ipPathList = zkClient.getChildren(servicePath);
            log.info("ipPathList={}", ipPathList);
            for (String ipPath : ipPathList) {
                String[] serverAddress = StringUtils.split(ipPath, UrlConstants.VERTICAL_LINE);
                if (ArrayUtils.isNotEmpty(serverAddress)) {
                    String serverIp = serverAddress[0];
                    int serverPort = Integer.parseInt(serverAddress[1]);
                    int weight = Integer.parseInt(serverAddress[2]);
                    int workerThreads = Integer.parseInt(serverAddress[3]);
                    String group = serverAddress[4];


                    List<Provider> providerList = providerServiceMap.get(serviceName);
                    if (providerList == null) {
                        providerList = new ArrayList<>();
                    }
                    Provider provider = Provider.builder()
                            .serverIp(serverIp)
                            .serverPort(serverPort)
                            .weight(weight)
                            .workerThreads(workerThreads)
                            .groupName(group)
                            .build();
                    try {
                        provider.setServiceItf(ClassUtils.getClass(serviceName));
                    } catch (Exception e) {
                        log.error("get service interface class error", e);
                        throw new SRpcException("get service interface class error", e);
                    }
                    providerList.add(provider);
                    providerServiceMap.put(serviceName, providerList);
                } else {
                    log.debug("illegal service address, ignore it");
                }
            }
            // 监听本地服务变化，加入本地缓存
            subscribeChildChanges(servicePath, SERVICE_METADATA);
        }
        return providerServiceMap;
    }

    private RegisterCenterImpl() {
    }
}
