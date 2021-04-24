package com.miracle.rpc.spring.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启simple-rpc注解服务
 * @author miracle
 * @date 2021/4/19 19:24
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({SRpcImportSelector.class})
public @interface EnableSRpc {

}
