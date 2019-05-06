package com.java.study.nio.course1;

import java.io.IOException;
import java.net.Socket;

public interface HttpServerHandler extends Runnable{

	void handle(Socket socket) throws IOException;
	
}
