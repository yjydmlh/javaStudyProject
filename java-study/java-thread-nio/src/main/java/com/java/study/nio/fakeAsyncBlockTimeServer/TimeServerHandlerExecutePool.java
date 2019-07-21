package com.java.study.nio.fakeAsyncBlockTimeServer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TimeServerHandlerExecutePool {

	private ExecutorService executor;
	
	public TimeServerHandlerExecutePool(int maxPoolSize,int queueSize){
		executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), maxPoolSize, 120l, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueSize));
	}
	
	public void execute(Runnable task){
		executor.execute(task);
	}
	
}
