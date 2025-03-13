package com.java.study.java8.gson;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.function.Consumer;

public class GsonUtil {
    // 静态单例模式初始化Gson实例（线程安全）
    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss") // 默认日期格式
            .serializeNulls() // 默认序列化null值（可选）
            .create();

    // 私有构造器防止实例化
    private GsonUtil() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    // === 基础转换方法 ===

    /**
     * 对象转JSON字符串（支持基本类型、对象、集合等）
     */
    public static String toJson(Object src) {
        return GSON.toJson(src);
    }

    /**
     * JSON字符串转对象（不支持泛型集合）
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /**
     * JSON字符串转对象（支持泛型，如List<User>、Map<String, Object>等）
     */
    public static <T> T fromJson(String json, Type typeOfT) {
        return GSON.fromJson(json, typeOfT);
    }

    // === 辅助方法（简化常见场景） ===

    /**
     * JSON字符串转List（自动推导元素类型）
     * @param elementType 集合元素类型（如User.class）
     */
    public static <T> List<T> jsonToList(String json, Class<T> elementType) {
        Type listType = new TypeToken<List<T>>(){}.getType();
        return GSON.fromJson(json, listType);
    }

    /**
     * JSON字符串转Map（自动推导键值类型）
     * @param keyType 键类型（如String.class）
     * @param valueType 值类型（如User.class）
     */
    public static <K, V> Map<K, V> jsonToMap(String json, Class<K> keyType, Class<V> valueType) {
        Type mapType = new TypeToken<Map<K, V>>(){}.getType();
        return GSON.fromJson(json, mapType);
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
        System.out.println(json); // {"name":"张三","birthDate":"2024-03-13 00:16:32"}

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
