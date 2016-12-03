package org.java.courses.leaderus.c7;

import java.util.List;

import org.java.courses.leaderus.util.UnsafeUtil;

import com.sun.management.VMOption;

import sun.misc.Unsafe;
import sun.management.*;

@SuppressWarnings("restriction")
public class Section7 {

	public static Unsafe unsafe = UnsafeUtil.getInstance();

	public static void main(String[] args) throws InstantiationException {
		// memPageTest();
		 unsafeNewObj();
//		pointCompressTest();
	}

	/**
	 * 获取VM参数
	 */
	public static void pointCompressTest() {
		HotSpotDiagnostic hsd = new HotSpotDiagnostic();
		VMOption vmo = hsd.getVMOption("UseCompressedOops");
		System.out.println(vmo.getValue());
		// List<VMOption> vmos = hsd.getDiagnosticOptions();
		// for (VMOption vmOption : vmos) {
		// System.out.println(vmOption.getName());
		// }
	}

	/**
	 * 
	 * 1 测试大内存页面与普通内存页面的性能差距，并做出解释
	 * 
	 * jvm大内存页默认是启用的，如果没有启用可以使用参数启用：-XX:+UseCompressedOops
	 * 
	 */
	public static void memPageTest() {
		System.out.println("pageS	ize=" + unsafe.pageSize());
		long start = System.currentTimeMillis();
		int[][] res = new int[2048][2048];
		int[][] mul1 = new int[2048][2048];
		for (int i = 0; i < 2048; i++) {
			for (int j = 0; j < 2048; j++) {
				for (int k = 0; k < 2048; k++) {
					res[i][j] += mul1[i][k] * mul1[k][j];
				}
			}
		}
		System.out.println("cost time:" + (System.currentTimeMillis() - start) / 1000);
	}

	/**
	 * 
	 * 
	 * 2 public class MyClass private final long a;
	 * 
	 * static { a=10; } Unsafe创建上述类的一个实例，看看a是多少，并做解释 ，
	 * final类型的变量只能在定义的时候或者静态代码段里初始化，初始化以后值不可改变
	 * unsafe.allocateInstance函数不调用构造函数直接创建一个类的实例
	 * 
	 */
	public static void unsafeNewObj() throws InstantiationException {
		MyClass obj = (MyClass) unsafe.allocateInstance(MyClass.class);
		System.out.println(obj.getA());
	}

}

class MyClass {
	
	private final static long a;

	public long getA() {
		return a;
	}

	static {
		a = 10;
		System.out.println("static segment a=" + a);
	}
}
