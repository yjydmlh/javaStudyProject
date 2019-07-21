package com.java.study.thread.volatileTest;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VolatileTest {

	private volatile int x;
	
	public static void main(String[] args) {
		VolatileTest vt = new VolatileTest();
		vt.setX(0);
		AddThread at = new AddThread(vt);
		Thread atThread = new Thread(at);
		
		atThread.start();
		
		for (int i = 0; i < 10; i++) {
			Thread gtThread1 = new Thread(new GetThread(vt));
			gtThread1.start();
		}
		
//		Thread gtThread1 = new Thread(new GetThread(vt));
//		Thread gtThread2 = new Thread(new GetThread(vt));
//		Thread gtThread3 = new Thread(new GetThread(vt));
		
		
//		gtThread1.start();
//		gtThread2.start();
//		gtThread3.start();
	}

}

@Setter
@Getter
class GetThread implements  Runnable{

	private VolatileTest vt;
	
	public GetThread(VolatileTest vt){
		this.vt = vt;
	}
	
	@Override
	public void run() {
		
		while (true) {
			try {
				Thread.sleep(1000);
				System.out.println("当前线程为：" + Thread.currentThread().getId()+Thread.currentThread().getName() + ",当前vt的值为：" + vt.getX());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
	}
	
}

@Setter
@Getter
class AddThread implements Runnable{

	private VolatileTest vt;
	
	public AddThread(VolatileTest vt){
		this.vt = vt;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
				vt.setX(vt.getX()+1);
			} catch (InterruptedException e) {
				
			}
		}
	}
	
}