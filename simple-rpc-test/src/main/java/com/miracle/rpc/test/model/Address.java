package com.miracle.rpc.test.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/23 19:59
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Address implements Serializable {

    private String addressCode;

    private String addressName;
}
