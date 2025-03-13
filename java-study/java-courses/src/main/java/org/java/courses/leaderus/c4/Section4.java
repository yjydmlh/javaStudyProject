//package org.java.courses.leaderus.c4;
//
////import sun.misc.Unsafe;
//
//public class Section4 {
//
//	public static void main(String[] args) {
////		Unsafe.getUnsafe().compareAndSwapLong(arg0, arg1, arg2, arg3)
////		Unsafe unsafe = Unsafe.getUnsafe();
//		testA();
//	}
//
//	public static void testA(){
//		VolatileBean vb = new VolatileBean();
//		Thread ta = new Thread(new ThreadA(vb));
//		ta.start();
//		Thread tb = new Thread(new ThreadB(vb));
//		tb.start();
//	}
//
//}
//
//class VolatileBean {
//	public  boolean s1=true;
//	public  int s2;
//	public  int s3;
//}
//
//class ThreadA implements Runnable {
//
//	private VolatileBean vb;
//
//	public ThreadA(VolatileBean vb){
//		this.vb=vb;
//	}
//
//	@Override
//	public void run() {
//		while (true) {
//			if (vb.s1) {
//				vb.s3 += vb.s2;
//				System.out.println("s1="+vb.s1+",s3="+vb.s3+",s2="+vb.s2);
//			} else {
//				vb.s3 -= vb.s2;
//			}
//			try {
//				Thread.sleep(3000);
//			} catch (InterruptedException e) {
//			}
//		}
//	}
//
//}
//
//
//class ThreadB implements Runnable {
//
//	private VolatileBean vb;
//
//	public ThreadB(VolatileBean vb){
//		this.vb=vb;
//	}
//
//	@Override
//	public void run() {
//		vb.s2 += 5;
//	}
//
//}
