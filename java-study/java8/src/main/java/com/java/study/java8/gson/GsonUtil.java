package com.java.study.java8.gson;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class GsonUtil {

    // 基础配置构建器（统一复用）
    private static GsonBuilder baseBuilder() {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss") // 默认日期格式
                .serializeNulls() // 默认序列化null值（可选）
                // 添加大数处理适配器
                .registerTypeAdapter(Long.class, new SafeNumberTypeAdapter())
                .registerTypeAdapter(long.class, new SafeNumberTypeAdapter())
                .registerTypeAdapter(BigInteger.class, new BigIntegerTypeAdapter());
    }
    // 普通模式实例
    private static final Gson NORMAL_GSON = baseBuilder().create();
    // 美化模式实例（附加格式设置）
    private static final Gson PRETTY_GSON = baseBuilder()
            .setPrettyPrinting()   // 关键区别配置
            .serializeNulls()      // 可选的额外配置
            .create();
    /**
     * 标准化输出（默认）
     * 使用示例：JsonUtils.toJson(user)
     */
    public static String toJson(Object obj) {
        return NORMAL_GSON.toJson(obj);
    }
    /**
     * 美化输出
     * 使用示例：JsonUtils.pretty().toJson(user)
     */
    public static Gson pretty() {
        return PRETTY_GSON;
    }
    // 反序列化统一使用普通实例（与格式化无关）
    public static <T> T fromJson(String json, Class<T> clazz) {
        return NORMAL_GSON.fromJson(json, clazz);
    }


    // 私有构造器防止实例化
    private GsonUtil() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    // === 基础转换方法 ===

    /**
     * JSON字符串转对象（支持泛型，如List<User>、Map<String, Object>等）
     */
    public static <T> T fromJson(String json, Type typeOfT) {
        return NORMAL_GSON.fromJson(json, typeOfT);
    }

    // === 辅助方法（简化常见场景） ===

    /**
     * JSON字符串转List（自动推导元素类型）
     * @param elementType 集合元素类型（如User.class）
     */
    public static <T> List<T> jsonToList(String json, Class<T> elementType) {
        Type listType = new TypeToken<List<T>>(){}.getType();
        return NORMAL_GSON.fromJson(json, listType);
    }

    /**
     * JSON字符串转Map（自动推导键值类型）
     * @param keyType 键类型（如String.class）
     * @param valueType 值类型（如User.class）
     */
    public static <K, V> Map<K, V> jsonToMap(String json, Class<K> keyType, Class<V> valueType) {
        Type mapType = new TypeToken<Map<K, V>>(){}.getType();
        return NORMAL_GSON.fromJson(json, mapType);
    }

    // === 高级配置（可扩展） ===

    /**
     * 自定义Gson配置（如排除null值、自定义序列化器）
     * @param config 自定义配置函数
     */
    public static Gson getCustomGson(Consumer<GsonBuilder> config) {
        GsonBuilder builder = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss"); // 保留默认日期格式
        config.accept(builder);
        return builder.create();
    }

    public static String prettyPrint(Object obj) {
        return PRETTY_GSON.toJson(obj);
    }

    /* 原有序列化部分保持不变... */
    /**
     * 解析JSON字符串为JsonObject
     * @throws IllegalArgumentException 当非对象结构或解析失败时抛出
     * 示例：JsonUtils.parseJsonObject("{\"name\":\"John\"}")
     */
    public static JsonObject parseJsonObject(String json) {
        return parseJsonElement(json).getAsJsonObject();
    }
    /**
     * 解析JSON字符串为JsonArray
     * @throws IllegalArgumentException 当非数组结构或解析失败时抛出
     * 示例：JsonUtils.parseJsonArray("[1,2,3]")
     */
    public static JsonArray parseJsonArray(String json) {
        return parseJsonElement(json).getAsJsonArray();
    }
    /**
     * 通用解析方法（底层实现）
     */
    private static JsonElement parseJsonElement(String json) {
        try {
            return JsonParser.parseString(json);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("JSON解析失败: " + e.getMessage(), e);
        }
    }
    /**
     * 安全解析模式（不抛异常）
     * 示例：Optional<JsonObject> = JsonUtils.tryParseJsonObject(invalidJson)
     */
    public static Optional<JsonObject> tryParseJsonObject(String json) {
        try {
            return Optional.of(parseJsonObject(json));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    /**
     * 带类型转换的深度解析
     * 示例：List<User> users = JsonUtils.parseAsList(json, User.class)
     */
    public static <T> List<T> parseAsList(String json, Class<T> elementType) {
        return NORMAL_GSON.fromJson(json, TypeToken.getParameterized(List.class, elementType).getType());
    }

    /**
     * Long类型智能转换适配器（处理基本类型long和包装类Long）
     */
    private static class SafeNumberTypeAdapter extends TypeAdapter<Number> {
        private static final long MAX_SAFE_INTEGER = (long) Math.pow(2, 53) - 1;
        private static final long MIN_SAFE_INTEGER = -MAX_SAFE_INTEGER;
        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            long numValue = value.longValue();
            if (numValue > MAX_SAFE_INTEGER || numValue < MIN_SAFE_INTEGER) {
                // 超出安全范围时序列化为字符串
                out.value(String.valueOf(numValue));
            } else {
                // 安全范围内保持数字类型
                out.value(numValue);
            }
        }
        @Override
        public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            // 处理两种输入形式：数字或数字字符串
            if (in.peek() == JsonToken.STRING) {
                String numStr = in.nextString();
                return parseNumberString(numStr);
            } else {
                return (long) in.nextDouble(); // 处理原始数字类型
            }
        }
        private Long parseNumberString(String numStr) {
            try {
                // 仅当长度不超过15位时转为Long（防止超大数精度丢失）
                if (numStr.length() < 16 && numStr.matches("^-?\\d+$")) {
                    return Long.parseLong(numStr);
                }
                // 返回原始字符串（后续可以按BigInteger处理）
                throw new NumberFormatException("Exceed Long range");
            } catch (NumberFormatException e) {
                // 保留字符串原始值以供后续处理
                throw new JsonSyntaxException("Long value out of range", e);
            }
        }
    }
    /**
     * 超大整数处理适配器（适用于BigInteger）
     */
    private static class BigIntegerTypeAdapter extends TypeAdapter<BigInteger> {
        @Override
        public void write(JsonWriter out, BigInteger value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            // 检查是否超过JS安全范围
            if (value.abs().compareTo(BigInteger.valueOf(SafeNumberTypeAdapter.MAX_SAFE_INTEGER)) > 0) {
                out.value(value.toString());
            } else {
                out.value(value);
            }
        }
        @Override
        public BigInteger read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            try {
                if (in.peek() == JsonToken.STRING) {
                    return new BigInteger(in.nextString());
                } else {
                    return BigInteger.valueOf((long) in.nextDouble());
                }
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException("Invalid big integer", e);
            }
        }
    }


    // === 示例使用方法 ===
    // （以下是测试用例，实际使用时需自行实现）

    // 测试实体类
    @Getter
    @Setter
    public static class User {
        private String name;
        private Date birthDate;
        // 构造器、Getter/Setter省略
    }

    public static void main(String[] args) {
        // 对象转JSON
        User user = new User();
        user.setName("张三");
        user.setBirthDate(new Date());
        String json = GsonUtil.toJson(user);
        System.out.println(json);
        System.out.println(GsonUtil.prettyPrint(user)); // {"name":"张三","birthDate":"2024-03-13 00:16:32"}

        // JSON转对象
        User user2 = GsonUtil.fromJson(json, User.class);

        // 转换泛型List
        List<User> userList = List.of(user);
        String listJson = GsonUtil.toJson(userList);
        List<User> users = GsonUtil.jsonToList(listJson, User.class);

        // 转换泛型Map
        Map<String, User> userMap = Map.of("user1", user);
        String mapJson = GsonUtil.toJson(userMap);
        Map<String, User> map = GsonUtil.jsonToMap(mapJson, String.class, User.class);

        // 自定义配置示例（排除null值）
        Gson customGson = GsonUtil.getCustomGson(builder -> builder.serializeNulls());
    }
}
