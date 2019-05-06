package com.java.study.nio.course1;

import java.io.IOException;
import java.net.Socket;

public abstract class AbstractHttpServerHandler implements HttpServerHandler {

	
	private Socket socket;
	
	
	public AbstractHttpServerHandler(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			handle(socket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
