package com.java.study.nio.fakeAsyncBlockTimeServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.java.study.nio.SyncBlockTimeServer.TimeServerHandler;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class TimeServer {

	public static void main(String[] args) throws IOException {
		int port = 8080;
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			System.out.println("time server is start in port:"+port);
			Socket socket= null;
			TimeServerHandlerExecutePool executor = new TimeServerHandlerExecutePool(50, 10000);
			while(true){
				socket = server.accept();
				executor.execute(new TimeServerHandler(socket));
			}
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}finally{
			if(server != null){
				System.out.println("time server close");
				server.close();
				server = null;
			}
		}
	}

}
