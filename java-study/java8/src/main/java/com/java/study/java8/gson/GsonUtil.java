package com.java.study.java8.gson;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gson工具类
 * 提供JSON序列化和反序列化的功能
 */
@Slf4j
public class GsonUtil {

    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // 默认Gson实例
    private static final Gson GSON = new GsonBuilder()
            .setDateFormat(DATE_FORMAT)
            .serializeNulls()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();
    
    // 美化输出Gson实例
    private static final Gson PRETTY_GSON = new GsonBuilder()
            .setDateFormat(DATE_FORMAT)
            .serializeNulls()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .setPrettyPrinting()
            .create();
    
    /**
     * 对象转JSON字符串
     */
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }
    
    /**
     * 对象转JSON字符串（美化输出）
     */
    public static String prettyJson(Object obj) {
        return PRETTY_GSON.toJson(obj);
    }
    
    /**
     * JSON字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return GSON.fromJson(json, clazz);
        } catch (Exception e) {
            log.error("JSON反序列化失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * JSON字符串转对象（支持泛型）
     */
    public static <T> T fromJson(String json, Type typeOfT) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return GSON.fromJson(json, typeOfT);
        } catch (Exception e) {
            log.error("JSON反序列化失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * JSON字符串转List
     */
    public static <T> List<T> jsonToList(String json, Class<T> elementType) {
        if (json == null || json.trim().isEmpty()) {
            return List.of();
        }
        try {
            Type listType = TypeToken.getParameterized(List.class, elementType).getType();
            return GSON.fromJson(json, listType);
        } catch (Exception e) {
            log.error("JSON转List失败: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * JSON字符串转Map
     */
    public static <K, V> Map<K, V> jsonToMap(String json, Class<K> keyType, Class<V> valueType) {
        if (json == null || json.trim().isEmpty()) {
            return Map.of();
        }
        try {
            Type mapType = TypeToken.getParameterized(Map.class, keyType, valueType).getType();
            return GSON.fromJson(json, mapType);
        } catch (Exception e) {
            log.error("JSON转Map失败: {}", e.getMessage());
            return Map.of();
        }
    }
    
    /**
     * 解析JSON字符串为JsonObject
     */
    public static JsonObject parseJsonObject(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new JsonObject();
        }
        try {
            JsonElement element = JsonParser.parseString(json);
            if (!element.isJsonObject()) {
                throw new JsonSyntaxException("JSON字符串不是对象格式");
            }
            return element.getAsJsonObject();
        } catch (Exception e) {
            log.error("JSON解析失败: {}", e.getMessage());
            return new JsonObject();
        }
    }
    
    /**
     * 解析JSON字符串为JsonArray
     */
    public static JsonArray parseJsonArray(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new JsonArray();
        }
        try {
            JsonElement element = JsonParser.parseString(json);
            if (!element.isJsonArray()) {
                throw new JsonSyntaxException("JSON字符串不是数组格式");
            }
            return element.getAsJsonArray();
        } catch (Exception e) {
            log.error("JSON解析失败: {}", e.getMessage());
            return new JsonArray();
        }
    }
    
    /**
     * 安全解析JSON对象
     */
    public static Optional<JsonObject> tryParseJsonObject(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            JsonElement element = JsonParser.parseString(json);
            if (!element.isJsonObject()) {
                return Optional.empty();
            }
            return Optional.of(element.getAsJsonObject());
        } catch (Exception e) {
            log.warn("JSON解析失败: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * 安全解析JSON数组
     */
    public static Optional<JsonArray> tryParseJsonArray(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            JsonElement element = JsonParser.parseString(json);
            if (!element.isJsonArray()) {
                return Optional.empty();
            }
            return Optional.of(element.getAsJsonArray());
        } catch (Exception e) {
            log.warn("JSON解析失败: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    // 私有构造器防止实例化
    private GsonUtil() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    // LocalDateTime类型适配器
    private static class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(value));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString(), formatter);
        }
    }
}
