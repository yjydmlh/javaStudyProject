package org.java.courses.leaderus.c7;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import org.java.courses.leaderus.util.UnsafeUtil;

import com.sun.management.VMOption;

import sun.misc.Unsafe;
import sun.management.*;

@SuppressWarnings("restriction")
public class Section7 {

	public static Unsafe unsafe = UnsafeUtil.getInstance();

	public static void main(String[] args) throws Exception {
		// memPageTest();
//		 unsafeNewObj();
//		pointCompressTest();
		buildRecordsInHeap();
//		buildRecordsOutHeap();
		
	}

	public static void myRecordSort() throws Exception{
		MyRecord mr = (MyRecord)unsafe.allocateInstance(MyRecord.class);
		MyRecord[] records = buildRecordsInHeap();
	}
	
	private static MyRecord[] buildRecordsOutHeap() throws InstantiationException {
		long start = System.currentTimeMillis();
		MyRecord[] records = new MyRecord[1000000];
		Random rand = new Random();
		for(int i=0;i<1000000;i++){
			MyRecord mr = (MyRecord)unsafe.allocateInstance(MyRecord.class);
			mr.col1 = rand.nextInt(Integer.MAX_VALUE);
			mr.col2 = (short) rand.nextInt(Integer.MAX_VALUE);
			mr.id = rand.nextInt(Integer.MAX_VALUE);
			records[i]=mr;
		}
		System.out.println("堆堆外创建耗时："+(System.currentTimeMillis() - start));
		return records;
	}
	
	private static MyRecord[] buildRecordsInHeap() throws InstantiationException {
		long start = System.currentTimeMillis();
		MyRecord[] records = new MyRecord[1000000];
		Random rand = new Random();
		for(int i=0;i<1000000;i++){
			MyRecord mr = new MyRecord();
			mr.col1 = rand.nextInt(Integer.MAX_VALUE);
			mr.col2 = (short) rand.nextInt(Integer.MAX_VALUE);
			mr.id = rand.nextInt(Integer.MAX_VALUE);
			records[i]=mr;
		}
		System.out.println("堆内创建耗时："+(System.currentTimeMillis() - start));
		return records;
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
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * 
	 */
	public static void unsafeNewObj() throws InstantiationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		System.out.println("is pointer  compress:"+UnsafeUtil.isPointerCompress());
		MyClass obj = (MyClass) unsafe.allocateInstance(MyClass.class);
		Field field = MyClass.class.getDeclaredField("a");
		long offset = unsafe.staticFieldOffset(field);
		System.out.println("get value of a by instance before modify ,a="+obj.getA());
		System.out.println("get value of a by unsafe before modify ,a="+unsafe.getLong(obj, offset));
		unsafe.putLong(obj, offset, 200);
		System.out.println("get value of a by instance after modify ,a="+obj.getA());
		System.out.println("get value of a by unsafe after modify ,a="+unsafe.getLong(obj, offset));
	}

}

class MyClass {
	
	private final static long a=20;

	public MyClass(){
		System.out.println("constructor，a="+a);
	}
	
	public long getA() {
		return a;
	}
	static {
//		a = 10;
		System.out.println("static segment a=" + a);
	}
}
