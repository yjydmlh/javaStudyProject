package com.java.study.java8.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;

class GsonUtilTest {

    @Test
    void testBasicSerialization() {
        // 测试基本类型序列化
        String json = GsonUtil.toJson(123);
        assertEquals("123", json);

        // 测试对象序列化
        TestObject obj = new TestObject("test", 123);
        json = GsonUtil.toJson(obj);
        assertTrue(json.contains("\"name\":\"test\""));
        assertTrue(json.contains("\"value\":123"));
    }

    @Test
    void testPrettyJson() {
        TestObject obj = new TestObject("test", 123);
        String prettyJson = GsonUtil.prettyJson(obj);
        assertTrue(prettyJson.contains("\n")); // 美化输出应该包含换行符
        assertTrue(prettyJson.contains("\"name\""));
        assertTrue(prettyJson.contains("\"value\""));
    }

    @Test
    void testBasicDeserialization() {
        // 测试基本类型反序列化
        Integer num = GsonUtil.fromJson("123", Integer.class);
        assertEquals(123, num);

        // 测试对象反序列化
        String json = "{\"name\":\"test\",\"value\":123}";
        TestObject obj = GsonUtil.fromJson(json, TestObject.class);
        assertEquals("test", obj.name);
        assertEquals(123, obj.value);
    }

    @Test
    void testGenericDeserialization() {
        // 测试泛型反序列化
        String json = "{\"name\":\"test\",\"value\":123}";
        Type type = new TypeToken<TestObject>(){}.getType();
        TestObject obj = GsonUtil.fromJson(json, type);
        assertEquals("test", obj.name);
        assertEquals(123, obj.value);
    }

    @Test
    void testListSerialization() {
        List<TestObject> list = Arrays.asList(
            new TestObject("test1", 1),
            new TestObject("test2", 2)
        );
        String json = GsonUtil.toJson(list);
        List<TestObject> result = GsonUtil.jsonToList(json, TestObject.class);
        assertEquals(2, result.size());
        assertEquals("test1", result.get(0).name);
        assertEquals("test2", result.get(1).name);
    }

    @Test
    void testMapSerialization() {
        Map<String, TestObject> map = new HashMap<>();
        map.put("key1", new TestObject("test1", 1));
        map.put("key2", new TestObject("test2", 2));
        String json = GsonUtil.toJson(map);
        Map<String, TestObject> result = GsonUtil.jsonToMap(json, String.class, TestObject.class);
        assertEquals(2, result.size());
        assertEquals("test1", result.get("key1").name);
        assertEquals("test2", result.get("key2").name);
    }

    @Test
    void testJsonObjectParsing() {
        String json = "{\"name\":\"test\",\"value\":123}";
        JsonObject obj = GsonUtil.parseJsonObject(json);
        assertEquals("test", obj.get("name").getAsString());
        assertEquals(123, obj.get("value").getAsInt());
    }

    @Test
    void testJsonArrayParsing() {
        String json = "[1,2,3]";
        JsonArray arr = GsonUtil.parseJsonArray(json);
        assertEquals(3, arr.size());
        assertEquals(1, arr.get(0).getAsInt());
        assertEquals(2, arr.get(1).getAsInt());
        assertEquals(3, arr.get(2).getAsInt());
    }

    @Test
    void testNullHandling() {
        // 测试null值处理
        String json = GsonUtil.toJson(null);
        assertEquals("null", json);

        TestObject obj = GsonUtil.fromJson("null", TestObject.class);
        assertNull(obj);
    }

    @Test
    void testEmptyInput() {
        // 测试空字符串输入
        TestObject obj = GsonUtil.fromJson("", TestObject.class);
        assertNull(obj);

        List<TestObject> list = GsonUtil.jsonToList("", TestObject.class);
        assertTrue(list.isEmpty());

        Map<String, TestObject> map = GsonUtil.jsonToMap("", String.class, TestObject.class);
        assertTrue(map.isEmpty());

        JsonObject jsonObj = GsonUtil.parseJsonObject("");
        assertTrue(jsonObj.entrySet().isEmpty());

        JsonArray jsonArr = GsonUtil.parseJsonArray("");
        assertTrue(jsonArr.isEmpty());
    }

    @Test
    void testSafeParsing() {
        // 测试安全解析
        Optional<JsonObject> result = GsonUtil.tryParseJsonObject("invalid json");
        assertFalse(result.isPresent());

        Optional<JsonArray> arrayResult = GsonUtil.tryParseJsonArray("invalid json");
        assertFalse(arrayResult.isPresent());

        // 测试正确格式
        result = GsonUtil.tryParseJsonObject("{\"name\":\"test\"}");
        assertTrue(result.isPresent());
        assertEquals("test", result.get().get("name").getAsString());

        arrayResult = GsonUtil.tryParseJsonArray("[1,2,3]");
        assertTrue(arrayResult.isPresent());
        assertEquals(3, arrayResult.get().size());
    }

    @Test
    void testComplexObject() {
        // 测试复杂对象
        ComplexObject complex = new ComplexObject();
        complex.setName("test");
        complex.setValue(123);
        complex.setList(Arrays.asList("a", "b", "c"));
        complex.setMap(new HashMap<>());
        complex.getMap().put("key1", "value1");
        complex.getMap().put("key2", "value2");

        String json = GsonUtil.toJson(complex);
        ComplexObject result = GsonUtil.fromJson(json, ComplexObject.class);
        
        assertEquals("test", result.getName());
        assertEquals(123, result.getValue());
        assertEquals(3, result.getList().size());
        assertEquals(2, result.getMap().size());
        assertEquals("value1", result.getMap().get("key1"));
        assertEquals("value2", result.getMap().get("key2"));
    }

    @Test
    void testBigNumber() {
        // 测试大数字
        // 测试超过JS安全范围的Long值
        Long bigLong = 9007199254740991L; // 2^53 - 1
        String json = GsonUtil.toJson(bigLong);
        Long result = GsonUtil.fromJson(json, Long.class);
        assertEquals(bigLong, result);

        // 测试BigInteger
        BigInteger bigInt = new BigInteger("123456789012345678901234567890");
        json = GsonUtil.toJson(bigInt);
        BigInteger bigIntResult = GsonUtil.fromJson(json, BigInteger.class);
        assertEquals(bigInt, bigIntResult);

        // 测试包含大数字的对象
        BigNumberObject bigNumberObj = new BigNumberObject();
        bigNumberObj.setBigLong(bigLong);
        bigNumberObj.setBigInteger(bigInt);
        json = GsonUtil.toJson(bigNumberObj);
        BigNumberObject objResult = GsonUtil.fromJson(json, BigNumberObject.class);
        assertEquals(bigLong, objResult.getBigLong());
        assertEquals(bigInt, objResult.getBigInteger());
    }

    // 测试用的内部类
    private static class TestObject {
        public String name;
        public int value;

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    // 复杂测试对象
    private static class ComplexObject {
        private String name;
        private int value;
        private List<String> list;
        private Map<String, String> map;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public List<String> getList() {
            return list;
        }

        public void setList(List<String> list) {
            this.list = list;
        }

        public Map<String, String> getMap() {
            return map;
        }

        public void setMap(Map<String, String> map) {
            this.map = map;
        }
    }

    // 大数字测试对象
    private static class BigNumberObject {
        private Long bigLong;
        private BigInteger bigInteger;

        public Long getBigLong() {
            return bigLong;
        }

        public void setBigLong(Long bigLong) {
            this.bigLong = bigLong;
        }

        public BigInteger getBigInteger() {
            return bigInteger;
        }

        public void setBigInteger(BigInteger bigInteger) {
            this.bigInteger = bigInteger;
        }
    }
} 