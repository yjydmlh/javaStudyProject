package com.java.study.java8.collection;

import java.util.stream.Stream;

public class Java8Stream {

	public static void main(String[] args) {

		Stream<Integer> intStream = Stream.of(1, 2, 3, 45, 6);
		Stream<String> strStream = Stream.of("taobao");
		Stream<Double> dblStream = Stream.generate(Math::random);
		

	}

}
