package com.miracle.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 服务请求者信息
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 9:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invoker implements Serializable {

    private Class<?> targetItf;

    private String remoteAppKey;

    private String groupName;
}
