package com.java.study.nio.SyncBlockTimeServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
			while(true){
				socket = server.accept();
				new Thread(new TimeServerHandler(socket)).start();;
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
