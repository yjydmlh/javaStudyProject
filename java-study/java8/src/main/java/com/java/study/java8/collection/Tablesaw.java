package com.java.study.java8.collection;


import cn.hutool.http.HttpUtil;
import com.google.common.collect.Lists;
import de.unknownreality.dataframe.DataFrame;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class Tablesaw {

    public static void main(String[] args) {
        BigDecimal s = BigDecimal.ZERO;
        BigDecimal b = BigDecimal.TEN;
        for (int i = 0; i < 10; i++){
            s = s.add(b);
        }
        System.out.println(s.toString());
        System.out.println(b.toString());
        BigDecimal a =  s.add(b);
        System.out.println(a.toString());
    }

}
@Data
class User{
    private Long id;
    private String name;
    private Integer age;
}