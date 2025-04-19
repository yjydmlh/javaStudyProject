package com.java.study.java8.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

@Slf4j
public class JacksonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectWriter PRETTY_WRITER;

    static {
        // 统一日期时间格式
        final String dateTimePattern = "yyyy-MM-dd HH:mm:ss";
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);

        // 配置Java8日期时间模块
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // LocalDateTime序列化/反序列化
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        // LocalDate序列化/反序列化（按需添加其他类型）
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_DATE));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_DATE));

        MAPPER.registerModule(javaTimeModule)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setDateFormat(new SimpleDateFormat(dateTimePattern))
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 初始化美化输出的Writer
        PRETTY_WRITER = MAPPER.writerWithDefaultPrettyPrinter();
    }

    private JacksonUtil() {
        throw new UnsupportedOperationException("JacksonUtil is a utility class and cannot be instantiated");
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    public static String toJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error serializing object to JSON", e);
            return null;
        }
    }

    public static String toJsonStringPretty(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return PRETTY_WRITER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error pretty serializing object to JSON", e);
            return null;
        }
    }

    public static <T> T parseObject(String json, Class<T> clazz) {
        if (isEmpty(json) || clazz == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            log.error("Error deserializing JSON to {}: {}", clazz.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        if (isEmpty(json) || typeReference == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            log.error("Error deserializing JSON to TypeReference: {}", e.getMessage(), e);
            return null;
        }
    }

    public static <T> T parseObject(String json, JavaType javaType) {
        if (isEmpty(json) || javaType == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            log.error("Error deserializing JSON to JavaType: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * JSON字符串转Map
     * @param json JSON字符串
     * @param keyType Map键类型
     * @param valueType Map值类型
     * @return 转换后的Map（失败返回空Map）
     */
    public static <K, V> Map<K, V> parseMap(String json, Class<K> keyType, Class<V> valueType) {
        if (isEmpty(json) || keyType == null || valueType == null) {
            return Map.of();
        }
        try {
            JavaType mapType = buildMapType(Map.class, keyType, valueType);
            return MAPPER.readValue(json, mapType);
        } catch (IOException e) {
            log.error("JSON转Map失败: {}", e.getMessage());
            return Map.of();
        }
    }

    public static JavaType buildCollectionType(Class<? extends Collection> collectionClass, Class<?> elementClass) {
        return MAPPER.getTypeFactory().constructCollectionType(collectionClass, elementClass);
    }

    public static JavaType buildMapType(Class<? extends Map> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return MAPPER.getTypeFactory().constructMapType(mapClass, keyClass, valueClass);
    }

    private static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }
}