package com.java.study.java8.gson;

public class GsonTest {

    public static void main(String[] args) {
        Long bigLong = 9007199254740991L; // 2^53 - 1
        String json = GsonUtil.toJson(bigLong);
        System.out.println(json);
        Long result = GsonUtil.fromJson(json, Long.class);
        System.out.println(result);
        Long result2 = GsonUtil.fromJson(Long.MAX_VALUE+"", Long.class);
        System.out.println(result2);
        System.out.println(GsonUtil.toJson(Long.MAX_VALUE));
    }

}
