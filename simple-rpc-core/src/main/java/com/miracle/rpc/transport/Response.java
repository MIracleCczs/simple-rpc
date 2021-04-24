package com.miracle.rpc.transport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 返回信息
 * @author miracle
 * @date 2021/4/16 16:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Response implements Serializable {

    private Long invokeTimeout;
    private Object result;
    private String uniqueKey;
}
