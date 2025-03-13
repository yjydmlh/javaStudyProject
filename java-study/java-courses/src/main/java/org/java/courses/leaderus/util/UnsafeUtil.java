//package org.java.courses.leaderus.util;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Modifier;
//
//import com.google.common.base.Preconditions;
//import com.sun.management.VMOption;
//
////import sun.management.HotSpotDiagnostic;
////import sun.misc.Unsafe;
//
//@SuppressWarnings("restriction")
//public class UnsafeUtil {
//
//	private static Unsafe unsafe;
//
//	static {
//		try {
//			Field field = Unsafe.class.getDeclaredField("theUnsafe");
//			field.setAccessible(true);
//			unsafe = (Unsafe) field.get(null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static Unsafe getInstance() {
//		if (unsafe == null) {
//			try {
//				Field field = Unsafe.class.getDeclaredField("theUnsafe");
//				field.setAccessible(true);
//				unsafe = (Unsafe) field.get(null);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return unsafe;
//	}
//
//	/**
//	 * 获取第一个元素的偏移地址
//	 *
//	 * @param clas
//	 * @return
//	 */
//	public static long getFirstFieldOffset(Class<?> clas) {
//		return unsafe.objectFieldOffset(clas.getFields()[1]);
//	}
//
//	/**
//	 * 打印field的offset
//	 *
//	 * @param clas
//	 */
//	public static void printFieldOffset(Class<?> clas) {
//		Field[] fields = clas.getDeclaredFields();
//		for (int i = 0; i < fields.length; i++) {
//			System.out.println(unsafe.objectFieldOffset(fields[i]));
//		}
//	}
//
//	public static long sizeOf(Class<?> clazz) {
//		long maxSize = headerSize(clazz);
//
//		while (clazz != Object.class) {
//			for (Field f : clazz.getDeclaredFields()) {
//				if ((f.getModifiers() & Modifier.STATIC) == 0) {
//					long offset = unsafe.objectFieldOffset(f);
//					if (offset > maxSize) {
//						// Assume 1 byte of the field width. This is ok as it
//						// gets padded out at the end
//						maxSize = offset + 1;
//					}
//				}
//			}
//			clazz = clazz.getSuperclass();
//		}
//
//		// The whole class always pads to a 8 bytes boundary, so we round up to
//		// 8 bytes.
//		return roundUpTo8(maxSize);
//	}
//
//	public static long headerSize(Object obj) {
//		return headerSize(obj.getClass());
//	}
//
//	 /**
//	   * Returns the size of the header for an instance of this class (in bytes).
//	   *
//	   * <p>More information <a href="http://www.codeinstructions.com/2008/12/java-objects-memory-structure.html">http://www.codeinstructions.com/2008/12/java-objects-memory-structure.html</a>
//	   * and <a href="http://stackoverflow.com/a/17348396/88646">http://stackoverflow.com/a/17348396/88646</a>
//	   *
//	   * <p><pre>
//	   * ,------------------+------------------+------------------ +---------------.
//	   * |    mark word(8)  | klass pointer(4) |  array size (opt) |    padding    |
//	   * `------------------+------------------+-------------------+---------------'
//	   * </pre>
//	   *
//	   * @param clazz
//	   * @return
//	   */
//	  public static long headerSize(Class<?> clazz) {
//	    Preconditions.checkNotNull(clazz);
//	    // TODO Should be calculated based on the platform
//	    // TODO maybe unsafe.addressSize() would help?
//	    long len = 12; // JVM_64 has a 12 byte header 8 + 4 (with compressed pointers on)
//	    if (clazz.isArray()) {
//	      len += 4;
//	    }
//	    return len;
//	  }
//
//	private static long roundUpTo8(final long number) {
//		return ((number + 7) / 8) * 8;
//	}
//
//	/**
//	 * 计算对象大小
//	 *
//	 * @param obj
//	 * @return
//	 */
//	public static long objSize(Object obj) {
//		long size = 12l;
//		if (obj == null) {
//			return 0l;
//		}
//		Field[] fields = obj.getClass().getDeclaredFields();
//		if (fields.length == 0) {
//			return size;
//		}
//		for (int i = 0; i < fields.length; i++) {
//			Field field = fields[i];
//		}
//		return size;
//	}
//
//	/**
//	 * 判断指针是否压缩
//	 *
//	 * @return
//	 */
//	public static boolean isPointerCompress() {
//		HotSpotDiagnostic hsd = new HotSpotDiagnostic();
//		VMOption vmo = hsd.getVMOption("UseCompressedOops");
//		return vmo.getValue().equals("true");
//	}
//
//	public static long getObjectReferenceAddress(Object obj) {
//		Object helperArray[] = new Object[1];
//		helperArray[0] = obj;
//		long baseOffset = unsafe.arrayBaseOffset(Object[].class);
//		// target的绝对地址
//		long addressOfObject = unsafe.getLong(helperArray, baseOffset);
//		if (isPointerCompress()) {
//			return addressOfObject * 8;
//		}
//		return addressOfObject;
//	}
//
//}
