package com.miracle.rpc.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

/**
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 9:41
 */
@Slf4j
public class IpUtil {

    /**
     * 获取服务端IP
     * @return
     */
    public static String getLocalIP() {
        StringBuilder sb = new StringBuilder();
        try {
            InetAddress addr = InetAddress.getLocalHost();
            byte[] ipAddr = addr.getAddress();
            for (int i = 0; i < ipAddr.length; i++) {
                if (i > 0) {
                    sb.append(".");
                }
                sb.append(ipAddr[i] & 0xFF);
            }
        } catch (Exception var) {
            log.error("IpUtil get local ip error", var);
        }
        return sb.toString();
    }
}
