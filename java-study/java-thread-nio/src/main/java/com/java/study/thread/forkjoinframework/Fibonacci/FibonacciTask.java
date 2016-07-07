package com.java.study.thread.forkjoinframework.Fibonacci;

import java.util.concurrent.RecursiveTask;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FibonacciTask extends RecursiveTask<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -911034629955502001L;

	private int n;

	public FibonacciTask(int n) {
		this.n=n;
	}

	@Override
	protected Integer compute() {
		int rs = 0;
		if(n<=1){
			rs=1;
		}else{
			FibonacciTask left = new FibonacciTask(n-1);
			FibonacciTask right = new FibonacciTask(n-1);
			left.fork();
			right.fork();
			rs = left.join()+right.join();
		}
		return rs;
	}

}
