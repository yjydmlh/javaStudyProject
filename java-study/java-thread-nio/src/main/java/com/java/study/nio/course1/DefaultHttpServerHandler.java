package com.java.study.nio.course1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.Socket;

public class DefaultHttpServerHandler  extends AbstractHttpServerHandler{

	public DefaultHttpServerHandler(Socket socket) {
		super(socket);
	}

	@Override
	public void handle(Socket socket) throws IOException {
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(socket.getInputStream()));
        String lineInput = null;
        String requestPage = null;
        while ((lineInput = lnr.readLine()) !=null){
            System.out.println("接受来自客户端的数据："+lineInput);
            if (lnr.getLineNumber() == 1){
                requestPage = lineInput.substring(lineInput.indexOf('/')-1,lineInput.lastIndexOf(' '));
            }else {
                if (lineInput.isEmpty()){
                    System.out.println("header  finished");
                    doResponseGet(requestPage,socket);
                }
            }
        }
	}

	private static void doResponseGet(String requestPage, Socket socket) throws IOException {
        System.out.println("do response get,requestPage="+requestPage);
        final String WEB_ROOT = "c:";
        File theFile = new File(WEB_ROOT,requestPage);
        OutputStream out = socket.getOutputStream();
        if (theFile.exists() && theFile.isFile()){
            InputStream fileIn = new FileInputStream(theFile);
            byte[] buf = new byte[fileIn.available()];
            fileIn.read(buf);
            fileIn.close();
            out.write(buf);
            out.flush();
            out.close();
            System.out.println("request complete");
            System.out.println("文件存在");
        }else{
            System.out.println("文件不存在");
            String msg = " I can't  find bao zang ....cry...\r\n";
            String response = "HTTP/1.1 200 OK\r\n";
            response+="Server:yjydmlh Server/0.1\r\n";
            response+="Content-Length: "+(msg.length()-4)+"\r\n";
            response+="asdffsadfi\r\n";
            response+="\r\n";
            response+=msg;
            out.write(response.getBytes());
            out.flush();
        }
    }
	
}
