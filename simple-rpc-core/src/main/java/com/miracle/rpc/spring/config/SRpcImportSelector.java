package com.miracle.rpc.spring.config;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author miracle
 * @date 2021/4/19 19:21
 */
public class SRpcImportSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        return new String[] {"com.miracle.rpc.spring.AnnotationServicesPublisher",
                "com.miracle.rpc.spring.ReferenceBeanPostProcessor"};
    }
}