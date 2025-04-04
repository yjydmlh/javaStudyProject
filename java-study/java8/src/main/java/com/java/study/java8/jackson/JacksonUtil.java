package com.java.study.java8.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;

@Slf4j
public class JacksonUtil {

    // 1. 创建静态 ObjectMapper 实例
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // ---- 配置 ObjectMapper ----

        // 配置 JSR310 Module (如果添加了依赖) - 处理 Java 8 日期/时间
        MAPPER.registerModule(new JavaTimeModule());
        // 默认不将日期/时间写为时间戳
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 配置旧版 Date 格式 (如果需要，并希望统一格式)
         MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 反序列化时，忽略 JSON 字符串中存在但 Java 对象实际没有的属性，防止报错
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 序列化时，忽略值为 null 的属性 (可选配置)
        // MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 序列化时，禁止序列化空 Pojo (无 public getter 或 field) 时报错 (可选配置)
        // MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        // 其他常用配置...
    }

    // 私有构造函数，防止实例化
    private JacksonUtil() {
    }

    /**
     * 获取预配置的 ObjectMapper 实例 (如果需要进行更复杂的自定义操作)
     *
     * @return 配置好的 ObjectMapper 实例
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    /**
     * 将 Java 对象序列化为 JSON 字符串
     *
     * @param obj 要序列化的对象
     * @return JSON 字符串，如果对象为 null 或序列化失败则返回 null
     */
    public static String toJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            // 实际项目中建议使用日志框架记录错误
            System.err.println("Error serializing object to JSON: " + e.getMessage());
            // 或者抛出自定义运行时异常
            // throw new RuntimeException("Error serializing object to JSON", e);
            return null;
        }
    }

    /**
     * 将 Java 对象序列化为格式化的 (pretty-printed) JSON 字符串
     *
     * @param obj 要序列化的对象
     * @return 格式化的 JSON 字符串，如果对象为 null 或序列化失败则返回 null
     */
    public static String toJsonStringPretty(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            System.err.println("Error pretty serializing object to JSON: " + e.getMessage());
            return null;
        }
    }


    /**
     * 将 JSON 字符串反序列化为指定 Class 类型的 Java 对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标对象的 Class
     * @param <T>   目标对象的类型
     * @return 反序列化后的对象，如果 JSON 为 null/空 或反序列化失败则返回 null
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty() || clazz == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) { // readValue 可能抛出 IOException 或其子类 JsonProcessingException
            System.err.println("Error deserializing JSON to Object: " + e.getMessage());
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为复杂的泛型类型 (如 List<User>, Map<String, Object>)
     * 使用 TypeReference 来保留泛型信息
     *
     * 用法示例: List<User> users = JacksonUtil.parseObject(json, new TypeReference<List<User>>() {});
     *
     * @param json          JSON 字符串
     * @param typeReference 包含泛型信息的目标类型引用
     * @param <T>           目标对象的类型
     * @return 反序列化后的对象，如果 JSON 为 null/空 或反序列化失败则返回 null
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty() || typeReference == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            System.err.println("Error deserializing JSON to generic type: " + e.getMessage());
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为复杂的泛型类型 (如 List<User>, Map<String, Object>)
     * 使用 JavaType 来构建泛型信息 (更灵活但使用稍复杂)
     *
     * 用法示例:
     * JavaType listType = JacksonUtil.buildCollectionType(List.class, User.class);
     * List<User> users = JacksonUtil.parseObject(json, listType);
     *
     * JavaType mapType = JacksonUtil.buildMapType(Map.class, String.class, Integer.class);
     * Map<String, Integer> map = JacksonUtil.parseObject(json, mapType);
     *
     * @param json     JSON 字符串
     * @param javaType 包含泛型信息的目标 JavaType
     * @param <T>      目标对象的类型
     * @return 反序列化后的对象，如果 JSON 为 null/空 或反序列化失败则返回 null
     */
    public static <T> T parseObject(String json, JavaType javaType) {
        if (json == null || json.trim().isEmpty() || javaType == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            System.err.println("Error deserializing JSON to JavaType: " + e.getMessage());
            return null;
        }
    }

    /**
     * 辅助方法：构建 Collection 类型的 JavaType
     * @param collectionClass 集合类型 (如 List.class)
     * @param elementClass  元素类型 (如 User.class)
     * @return 构建好的 JavaType
     */
    public static JavaType buildCollectionType(Class<?> collectionClass, Class<?> elementClass) {
        return MAPPER.getTypeFactory().constructCollectionType((Class<? extends Collection>) collectionClass, elementClass);
    }

    /**
     * 辅助方法：构建 Map 类型的 JavaType
     * @param mapClass   Map 类型 (如 Map.class)
     * @param keyClass   键类型 (如 String.class)
     * @param valueClass 值类型 (如 Integer.class)
     * @return 构建好的 JavaType
     */
    public static JavaType buildMapType(Class<?> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return MAPPER.getTypeFactory().constructMapType((Class<? extends Map>) mapClass, keyClass, valueClass);
    }

}
