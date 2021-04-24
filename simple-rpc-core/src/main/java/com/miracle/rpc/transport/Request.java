package com.miracle.rpc.transport;

import com.miracle.rpc.model.Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 请求信息
 * @author miracle
 * @date 2021/4/16 16:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request implements Serializable {

    private Provider provider;
    private Long invokeTimeout;
    private String invokeMethodName;
    private String uniqueKey;
    private Object[] args;
}