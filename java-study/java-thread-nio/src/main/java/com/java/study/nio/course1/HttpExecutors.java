package com.java.study.nio.course1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpExecutors {

	private static final ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
	
	public static void exec(Runnable task) {
		exec.execute(task);
	}
	
}
