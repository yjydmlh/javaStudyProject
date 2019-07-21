package com.java.study.thread.lock;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lombok.Getter;
import lombok.Setter;

public class LockTest {

	public static void main(String[] args) {
		Tick ticks = new Tick();
		ticks.setTickNO(20);
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock(false);
		Thread sell1 = new Thread(new Seller(ticks, lock.writeLock()),"窗口1");
		Thread sell2 = new Thread(new Seller(ticks, lock.writeLock()),"窗口2");
		Thread sell3 = new Thread(new Seller(ticks, lock.writeLock()),"窗口3");
		Thread sell4 = new Thread(new Seller(ticks, lock.writeLock()),"窗口4");
		
		sell1.start();
		sell2.start();
		sell3.start();
		sell4.start();
		
	}

}

@Setter
@Getter
class Tick{
	private int tickNO;
}

@Getter
@Setter
class Seller implements Runnable{

	private Tick tick;
	private Lock lock ;
	
	
	public Seller(Tick tick,Lock lock) {
		this.tick = tick;
		this.lock = lock;
	}

	@Override
	public void run() {
		try {
			
			while(this.tick.getTickNO()>0) {
				lock.lock();
				Random random = new Random();
				Thread.sleep(Math.abs(random.nextInt()%100));
				if(this.tick.getTickNO()>0) {
					tick.setTickNO(this.getTick().getTickNO() - 1);
					System.out.println(Thread.currentThread().getName()+"卖出了一张票,"+"票号为："+(tick.getTickNO()+1)+","+"还剩下"+tick.getTickNO()+"张票");
				}else {
					break;
				}
				lock.unlock();
				Thread.sleep(1000);
			}
			System.out.println("票卖完了");
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			
		}
	}
	
	
}