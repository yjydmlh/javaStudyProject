package org.java.courses;

public class Courses1 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// one();
//		two();
		int a = (int)123.9999999999999;
		System.out.println(a);
		double d = 0.1;
		System.out.println(d);
		System.out.println(Long.MAX_VALUE+9223372036l);
	}

	/**
	 * 2题 int a=-1024; 给出 a>>1与a>>>1的的结果，并且用位移方式图示解释
	 * 
	 * 
	 * 
	 */
	public static void two() {
		int a = -1024;
		System.out.println(a >> 1);
		System.out.println(a >>> 1);
	}

	/**
	 * 1题 byte ba=127; byte bb=ba<<2; System.out.println(bb);
	 * 这个为什么会出错？给出解释，并且纠正错误
	 * 
	 * 位移操作是针对整型的，Java中的数字默认都是整型的，所以会报编译错误,必须强转为byte类型
	 */
	public static void one() {
		byte ba = 127;
		byte bb = (byte) (ba << 0);
		System.out.println(bb);
	}

}
