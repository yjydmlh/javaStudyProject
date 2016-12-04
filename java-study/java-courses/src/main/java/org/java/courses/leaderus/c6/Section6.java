package org.java.courses.leaderus.c6;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.java.courses.leaderus.util.UnsafeUtil;
import org.osgi.framework.SynchronousBundleListener;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class Section6 {

	public static Unsafe unsafe;

	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws NoSuchFieldException, SecurityException, InstantiationException {
//		unsafeArray();
//		 unsafeArrayOffset();
//		System.out.println(1<<3);
		unsafeGetLongTest();
//		unsafeNewObjTest();
	}

	public static void unsafeNewObjTest() throws InstantiationException{
		Target t = (Target)unsafe.allocateInstance(Target.class);
		System.out.println(t.getId());
	}
	
	/**
	 * -XX:+UseCompressedOops
	 */
	public static void unsafeGetLongTest(){
		System.out.println(UnsafeUtil.isPointerCompress());
		Target helperArray[] 	= new Target[10];
		helperArray[0] 		= new Target();
//		long baseOffset 		= unsafe.arrayBaseOffset(Target[].class);
		//target的绝对地址
		long addressOfObject	= UnsafeUtil.getObjectReferenceAddress(new Target());
//		System.out.println(baseOffset);
		System.out.println(addressOfObject);
		System.out.println("addressSize="+unsafe.addressSize());
		//获取target的id的值
		if(UnsafeUtil.isPointerCompress()){
			//指针压缩，偏移量是12
			System.out.println(unsafe.getInt(addressOfObject+12));
		}else{
			//指针未压缩，偏移量是16
			System.out.println(unsafe.getInt(addressOfObject+16));
		}
	}
	
	public static void unsafeArrayOffset() {
		System.out.println("ArrayList array scale " + unsafe.arrayIndexScale(ArrayList[].class));
		System.out.println("MyObj array scale " + unsafe.arrayIndexScale(MyObj[].class));
		System.out.println("long array scale " + unsafe.arrayIndexScale(long[].class));
		System.out.println("boolean array scale " + unsafe.arrayIndexScale(boolean[].class));
		System.out.println("Boolean array scale " + unsafe.arrayIndexScale(Boolean[].class));
		System.out.println("int array scale " + unsafe.arrayIndexScale(int[].class));
		System.out.println("integer array scale " + unsafe.arrayIndexScale(Integer[].class));
		System.out.println("byte array scale " + unsafe.arrayIndexScale(byte[].class));
		System.out.println("short array scale " + unsafe.arrayIndexScale(short[].class));
		System.out.println("Long array scale " + unsafe.arrayIndexScale(Long[].class));
	}

	/**
	 * 
	 * 2 解释 unsafe.arrayBaseOffset
	 * 在64位JDK下，开启压缩与不开启指针压缩情况下的值，以及为什么不管传递byte[]还是int[]，还是自定义对象，都是同样的值？
	 * 
	 * 
	 */
	public static void unsafeArray() {
		byte[] ba = new byte[] {};
		long offset = unsafe.arrayBaseOffset(ba.getClass());
		int[] ia = new int[] {};
		long iaOffset = unsafe.arrayBaseOffset(ia.getClass());
		System.out.println(iaOffset);
		MyClass[] m = new MyClass[] {};
		long mOffset = unsafe.arrayBaseOffset(m.getClass());
		System.out.println(mOffset);
	}

	public static void printOffset() {
		Field[] fields = MyClass.class.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			long cOffset = unsafe.objectFieldOffset(fields[i]);
			System.out.println(fields[i] + ",offset=" + cOffset);
		}
	}

}

class Target{
	private int id=1000;
	public int getId(){
		return this.id;
	}
}

/**
 * 1 解释属性c的内存偏移地址（考虑内存布局与Padding的问题）
 * 
 * @author Administrator
 *
 */
class MyObj {
	private int a;
	private byte b;
	private int c;
}

class MyClass {
	private byte a;
	private int c;
	private boolean d;
	private long e;
	private Object f;
}