package org.java.courses.leaderus.c5;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;

import lombok.ToString;

/**
 * cas的aba问题和解决
 * @author Administrator
 *
 */
public class Section5 {

	public static void main(String[] args) throws InterruptedException {
//		atomicStamppedRefernceTest();
//		abaTest();
		abaProblem();
		asrSolution();
	}
	
	public static void asrSolution(){
		AtomicStampedReference<Integer> money = new AtomicStampedReference<Integer>(500, 1);
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				money.compareAndSet(500, 200, money.getStamp(), money.getStamp()+1);
				money.compareAndSet(200, 500, money.getStamp(), money.getStamp()+1);
			}
		});
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				boolean flag = money.compareAndSet(50, 200, money.getStamp(), money.getStamp()+1);
				System.out.println("修改结果："+flag);
			}
		});
		t1.start();
		t2.start();
	}
	
	/**
	 * ABA问题模拟
	 */
	public static void abaProblem(){
		AtomicInteger money = new AtomicInteger(50);
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				money.compareAndSet(50, 100);
				money.compareAndSet(100, 50);
			}
		});
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				boolean flag = money.compareAndSet(50, 200);
				System.out.println("修改结果："+flag);
			}
		});
		t1.start();
		t2.start();
	}
	
	
	
	public static void abaTest() throws InterruptedException{
		AtomicInteger atomicInt = new AtomicInteger(100);
        AtomicStampedReference atomicStampedRef = new AtomicStampedReference(100, 0);

                Thread intT1 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                                atomicInt.compareAndSet(100, 101);
                                atomicInt.compareAndSet(101, 100);
                        }
                });

                Thread intT2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                                try {
                                        TimeUnit.SECONDS.sleep(1);
                                } catch (InterruptedException e) {
                                }
                                boolean c3 = atomicInt.compareAndSet(100, 101);
                                System.out.println(c3); // true
                        }
                });

                intT1.start();
                intT2.start();
                intT1.join();
                intT2.join();

                Thread refT1 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                                try {
                                        TimeUnit.SECONDS.sleep(1);
                                } catch (InterruptedException e) {
                                }
                                atomicStampedRef.compareAndSet(100, 101, atomicStampedRef.getStamp(), atomicStampedRef.getStamp() + 1);
                                atomicStampedRef.compareAndSet(101, 100, atomicStampedRef.getStamp(), atomicStampedRef.getStamp() + 1);
                        }
                });

                Thread refT2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                                int stamp = atomicStampedRef.getStamp();
                                try {
                                        TimeUnit.SECONDS.sleep(2);
                                } catch (InterruptedException e) {
                                }
                                boolean c3 = atomicStampedRef.compareAndSet(100, 101, stamp, stamp + 1);
                                System.out.println(c3); // false
                        }
                });

                refT1.start();
                refT2.start();
	}
	
}
class ThreadA implements Runnable{

	private Map<String,ABATest> tm ;
	
	public ThreadA(Map<String,ABATest> abaTest){
		tm=abaTest;
	}
	
	@Override
	public void run() {
		synchronized (tm) {
			while(tm.get("flag") != null){
				tm.get("tm1").inc();
				System.out.println(tm.get("tm1").getValue());
				try {
					Thread.currentThread().wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(tm.get("tm1").getValue());
			}
		}
	}
}

class ThreadB implements Runnable{

	private Map<String,ABATest> tm ;
	
	public ThreadB(Map<String,ABATest> abaTest){
		tm=abaTest;
	}
	
	@Override
	public void run() {
		synchronized (tm) {
			ABATest tm2 = new ABATest();
			ABATest tm1 = tm.get("tm1");
			tm2.inc();
			tm2.inc(tm1.getValue());
			tm.put("tm1", tm2);
			tm1.inc();
			tm.put("tm1", tm1);
			System.out.println(tm.get("tm1").getValue());
			Thread.currentThread().notify();
			System.out.println(tm.get("tm1").getValue());
		}
	}
}


@ToString
class ABATest{
	
	
	public void inc(){
		value++;
	}

	public void inc(int v){
		value+=v;
	}
	
	private int value=3;

	public int getValue() {
		return value;
	}

	
}