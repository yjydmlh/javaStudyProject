package com.java.study.nio.SyncBlockTimeServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.joda.time.DateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class TimeServerHandler  implements Runnable{

	private Socket socket;
	
	public TimeServerHandler(Socket skt){
		this.socket = skt;
	}
	
	@Override
	public void run() {
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			out = new PrintWriter(this.socket.getOutputStream(),true);
			String currentTime = null;
			String body = null;
			while(true){
				body = in.readLine();
				if(body == null){
					break;
				}
				System.out.println("the time server receive order:"+body);
				currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? DateTime.now().toString():" bad order ";
				out.println(currentTime);
			}
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			if(in!=null){
				try {
					in.close();
				} catch (IOException e1) {
					log.error(e.getMessage(),e);
				}
			}
			if(out !=null){
				out.close();
				out = null;
			}
			if(this.socket != null){
				try {
					this.socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				this.socket = null;
			}
		}
		
	}

}
