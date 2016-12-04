package org.java.courses.leaderus.util;

import java.lang.reflect.Field;

import com.sun.management.VMOption;

import sun.management.HotSpotDiagnostic;
import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class UnsafeUtil {

	public static Unsafe unsafe;

	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Unsafe getInstance() {
		if (unsafe == null) {
			try {
				Field field = Unsafe.class.getDeclaredField("theUnsafe");
				field.setAccessible(true);
				unsafe = (Unsafe) field.get(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return unsafe;
	}

	/**
	 * 判断指针是否压缩
	 * 
	 * @return
	 */
	public static boolean isPointerCompress() {
		HotSpotDiagnostic hsd = new HotSpotDiagnostic();
		VMOption vmo = hsd.getVMOption("UseCompressedOops");
		return vmo.getValue().equals("true");
	}

	public static long getObjectReferenceAddress(Object obj) {
		Object helperArray[] = new Object[1];
		helperArray[0] = obj;
		long baseOffset = unsafe.arrayBaseOffset(Object[].class);
		// target的绝对地址
		long addressOfObject = unsafe.getLong(helperArray, baseOffset);
		if (isPointerCompress()) {
			return addressOfObject * 8;
		}
		return addressOfObject;
	}

}
