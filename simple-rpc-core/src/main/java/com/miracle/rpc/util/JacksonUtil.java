package com.miracle.rpc.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.miracle.rpc.serializer.FDateJsonDeserializer;
import com.miracle.rpc.serializer.FDateJsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/23 19:30
 */
@Slf4j
public class JacksonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        SimpleModule simpleModule = new SimpleModule("DateTimeModule", Version.unknownVersion());
        simpleModule.addSerializer(LocalDateTime.class, new FDateJsonSerializer());
        simpleModule.addDeserializer(LocalDateTime.class, new FDateJsonDeserializer());
        OBJECT_MAPPER.registerModule(simpleModule);
    }

    private static ObjectMapper getObjectMapperInstance() {
        return OBJECT_MAPPER;
    }

    /**
     * 对象转json
     * @param obj
     * @return
     */
    public static String objectToJson(Object obj) {
        String json = "";

        try {
            json = OBJECT_MAPPER.writeValueAsString(obj);
        } catch (IOException e) {
            log.error("jsonSerilFailed, obj is: {}", obj, e);
        }

        return json;
    }

    /**
     * json 转Object
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T jsonToObject(String json, Class<T> clazz) {
        Object obj = null;
        try {
            obj = OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            log.error(String.format("jsonDeserilFailed, json is: %s, class is : %s", new Object[]{json, clazz.toString()}), e);
        }
        return (T) obj;
    }
}
