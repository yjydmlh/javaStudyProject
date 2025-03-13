//package org.java.courses.leaderus;
//
//public class VolatileTest {
//
//	private volatile int p;
//
//
//
//	public int getP() {
//		return p;
//	}
//
//
//
//	public void setP(int p) {
//		this.p = p;
//	}
//
//
//
//	public static void main(String[] args) {
//		VolatileTest vt = new VolatileTest();
//		writeT(vt);
//		readT(vt);
//	}
//
//	public static void readT(VolatileTest vt){
//		Thread r = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				while(true){
//					System.out.println(vt.getP());
//					try {
//						Thread.sleep(2000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		});
//		r.start();
//	}
//
//	public static void writeT(VolatileTest vt){
//		Thread w = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				int i=0;
//				while(true){
//					vt.setP(i++);
//					try {
//						Thread.sleep(2000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		});
//		w.start();
//	}
//
//}
