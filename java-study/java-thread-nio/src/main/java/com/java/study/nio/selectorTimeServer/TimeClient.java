package com.java.study.nio.selectorTimeServer;

public class TimeClient {

	public static void main(String[] args) {
		int port=8080;
		new Thread(new TimeClientHandler("127.0.0.1", port),"timeClient-001").start();
	}

}
