package com.java.study.thread.forkjoinframework.Fibonacci;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class Fibonacci {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
    	ForkJoinPool pool = new ForkJoinPool();
    	for(int i=1;i<=10;i++){
    		FibonacciTask task = new FibonacciTask(i);
    		Future<Integer> future = pool.submit(task);
    		Integer rs = future.get();
    		System.out.println(rs);
    	}
    }

}
