package com.java.study.java8.jackson;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JacksonTest {

    public static void main(String[] args) throws JsonProcessingException {
        List<Object> animals = new ArrayList<>();
        animals.add(new Dog("Buddy", 5));
        animals.add(new Cat("Whiskers", true));

        ObjectMapper mapper = new ObjectMapper();
        // 配置Java8日期时间模块
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        final String dateTimePattern = "yyyy-MM-dd HH:mm:ss";
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
        // LocalDateTime序列化/反序列化
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

        mapper.registerModule(javaTimeModule);
        System.out.println(mapper.writeValueAsString(new Dog("Buddy", 5)));

        // 序列化：JSON 会包含 @type 字段
//        String json = JacksonUtil.toJsonString(animals);
//        System.out.println("Serialized with Type Info:\n" + json);
        /* 输出类似:
           [
             {"@type":"dog","name":"Buddy","boneCount":5},
             {"@type":"cat","name":"Whiskers","likesCream":true}
           ]
        */

        // 反序列化：即使期望是 Animal，也能正确解析为 Dog 和 Cat
//        List<Animal> deserializedAnimals = JacksonUtil.parseObject(json, new TypeReference<List<Animal>>() {});
//        System.out.println("\nDeserialized Animals:");
//        deserializedAnimals.forEach(System.out::println);
        /* 输出:
           Dog{name='Buddy', boneCount=5}
           Cat{name='Whiskers', likesCream=true}
        */
    }

}


// 基类/接口
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,        // 使用逻辑名称
        include = JsonTypeInfo.As.PROPERTY, // 作为属性包含
        property = "@type"                  // 属性名叫 @type
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Dog.class, name = "dog"), // 定义 Dog 的名称是 "dog"
        @JsonSubTypes.Type(value = Cat.class, name = "cat")  // 定义 Cat 的名称是 "cat"
})
@Getter
abstract class Animal {
    public String name;
    public LocalDateTime birthDate = LocalDateTime.now();
    protected Animal(String name) { this.name = name; }
    protected Animal() {} // 需要无参构造
}

// 子类 Dog
 @JsonTypeName("dog") // 也可以在这里定义名称，替代 @JsonSubTypes 中的指定
 @Getter
class Dog extends Animal {
    public int boneCount;

    public Dog(String name, int boneCount) { super(name); this.boneCount = boneCount; }
    private Dog() {} // 需要无参构造

    @Override
    public String toString() { return "Dog{name='" + name + "', boneCount=" + boneCount + '}'; }
}

// 子类 Cat
 @JsonTypeName("cat")
 @Getter
 @JsonTypeInfo(
         use = JsonTypeInfo.Id.NAME,        // 使用逻辑名称
         include = JsonTypeInfo.As.PROPERTY, // 作为属性包含
         property = "@type"                  // 属性名叫 @type
 )
class Cat extends Animal {
    public boolean likesCream;

    public Cat(String name, boolean likesCream) { super(name); this.likesCream = likesCream; }
    private Cat() {} // 需要无参构造

    @Override
    public String toString() { return "Cat{name='" + name + "', likesCream=" + likesCream + '}'; }
}