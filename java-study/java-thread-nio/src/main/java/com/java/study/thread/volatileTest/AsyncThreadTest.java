package com.java.study.thread.volatileTest;

import java.util.ArrayList;
import java.util.List;

public class AsyncThreadTest {

	public static void main(String[] args) throws InterruptedException {
		List<Integer> lst = new ArrayList<Integer>();
		lst.add(1);lst.add(2);lst.add(3);lst.add(4);lst.add(5);lst.add(6);lst.add(7);lst.add(8);lst.add(9);lst.add(10);lst.add(11);lst.add(12);
		lst.add(1);lst.add(2);lst.add(3);
		lst.add(1);lst.add(2);lst.add(3);
		lst.add(1);lst.add(2);lst.add(3);
		lst.add(1);lst.add(2);lst.add(3);
		lst.add(1);lst.add(2);lst.add(3);
		lst.add(1);lst.add(2);lst.add(3);
		lst.add(1);lst.add(2);lst.add(3);
		lst.add(1);lst.add(2);lst.add(3);
		List<Integer> tmp = new ArrayList<Integer>();
		for (int i = 0; i < lst.size(); i++) {
			tmp.add(lst.get(i));
			if ( (i+1)%10==0 || i==lst.size()-1) {
				System.out.println(tmp.size());
				tmp.clear();
			}
		}
		
	}

	class RunJob implements Runnable{

		private List<Integer> lst ;
		
		public RunJob(List<Integer> list) {
			this.lst = list;
		}
		
		@Override
		public void run() {
			System.out.println(this.lst.size());
		}
		
	}
	
	public  void asyncThreadTest() throws InterruptedException {
		List<Integer> lst = new ArrayList<Integer>();
		AsyncThreadTest tstAsyncThreadTest = new AsyncThreadTest();
		lst.add(1);lst.add(2);lst.add(3);lst.add(4);lst.add(5);lst.add(6);lst.add(7);lst.add(8);lst.add(9);lst.add(10);lst.add(11);lst.add(12);
		RunJob job = new RunJob(lst);
		Thread t = new Thread(job);
		t.start();
		Thread.sleep(5);
		lst.clear(); 
	}
	
	
}
