package org.java.courses.leaderus;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 定义Java类Salary {String name, int baseSalary, int bonus
 * },随机产生1万个实例，属性也随机产生（baseSalary范围是5-100万，
 * bonus为（0-10万），其中name长度为5，随机字符串，然后进行排序，
 * 排序方式为收入总和（baseSalary*13+bonus），
 * 输出收入最高的10个人的名单
 */
public class Class2 {

    public static void main(String[] args) {
        
    }

    public static void generate(){
        List<Salary>  l = new ArrayList<Salary>();
        Salary s = null;
        for(int i=0;i<10000;i++){
            s = new Salary();
//            s.setBonus(bonus);
        }
    }
    
}
@Getter
@Setter
class Salary{
    private String name;
    private int bonus;
}