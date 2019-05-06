package com.java.study.nio.course1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.LongAdder;

/**
 * Created by Administrator on 2017/6/16.
 */
public class HttpServerSyncBlock {

    public static  void  main(String [] args ) throws IOException{
        httpServer(80);
    }

    private static LongAdder counter = new LongAdder();
    
    public static void httpServer(int port) throws IOException{
            ServerSocket sever = new ServerSocket(port);
            while (true){
                Socket socket = sever.accept();
                System.out.println("server 启动成功");
                counter.add(1);
                System.out.println("第"+counter.longValue()+"个连接");
                DefaultHttpServerHandler handler = new DefaultHttpServerHandler(socket);
                HttpExecutors.exec(handler);
            }
    }
}
