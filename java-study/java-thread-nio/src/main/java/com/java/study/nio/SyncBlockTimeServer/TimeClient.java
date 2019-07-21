package com.java.study.nio.SyncBlockTimeServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeClient {

	public static void main(String[] args) {
		int port = 8080;
		Socket socket = null;
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			socket = new Socket("127.0.0.1", port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(),true);
			out.println("QUERY TIME ORDER");
			System.out.println("Send order 2 server succeed");
			String resp = in.readLine();
			System.out.println("Now is : "+resp);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		} finally{
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
					log.error(e.getMessage(),e);
				}
				in = null;
			}
			if(socket != null){
				try {
					socket.close();
				} catch (IOException e) {
					log.error(e.getMessage(),e);
				}
				socket = null;
			}
		}
	}

}
