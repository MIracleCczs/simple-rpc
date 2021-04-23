package com.miracle.rpc.serializer;

import org.apache.commons.lang3.StringUtils;

/**
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/23 19:34
 */
public enum SerializerTypeEnum {

    DEFAULT_SERIALIZER("1"),
    JACKSON_SERIALIZER("2"),
    FASTJSON_SERIALIZER("3"),
    HESSIAN_SERIALIZER("4"),
    PROTOBUF_SERIALIZER("5");

    private String typeCode;

    SerializerTypeEnum(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeCode() {
        return typeCode;
    }

    /**
     * 根据序列化方式获取
     * @param serializeType
     * @return
     */
    public static SerializerTypeEnum getByType(String serializeType) {

        if (StringUtils.isNotEmpty(serializeType)) {
            SerializerTypeEnum[] values = SerializerTypeEnum.values();
            for (SerializerTypeEnum value : values) {
                if (StringUtils.equals(value.getTypeCode(), serializeType)) {
                    return value;
                }
            }
        }
        return null;
    }
}
