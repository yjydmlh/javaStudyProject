package com.java.study.java8;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

public class LamdaExpression {

    public static void main(String[] args) {
        List<String> names = Arrays.asList("peter", "anna", "mike", "xenia");
        List<String> upNames = names.stream().map(name -> name.toUpperCase()).collect(Collectors.toList());
        names.forEach(System.out::println);
        upNames.forEach(System.out::println);
    }

    public static void functionInterface(){
//    	List<Long> list = Lists.newArrayList(1l,2l,3l,4l,5l,6l);
    	IFunctionSum<Long> function = list ->{
    		long sum = 0;
    		for (long item:list) {
				sum += item;
			}
			return sum;
    	};
    	long rs = function.sum(Lists.newArrayList(1l,2l,3l,4l,5l,6l));
    	System.out.println(rs);
    }
    
}
/**
 * java8中如果一个接口只有一个抽象方法，那么接口默认就是一个函数式接口，
 * 如果加上@FunctionalInterface注解，那么接口就只能定义一个抽象方法，定义多于一个抽象方法则会报编译错误
 * @author Administrator
 *
 * @param <T>
 */
@FunctionalInterface
interface IFunctionSum<T extends Number>{
	T sum(List<T> numbers);
//	T divide(List<T> numbers);
}