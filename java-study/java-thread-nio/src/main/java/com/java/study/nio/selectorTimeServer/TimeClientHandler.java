package com.java.study.nio.selectorTimeServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeClientHandler implements Runnable {

	
	private Selector selector;
	private SocketChannel sktChannel;
	
	private String host;
	private int port;
	
	private boolean stop = false;
	
	public TimeClientHandler(String host,int port) {
		try {
			selector = Selector.open();
			sktChannel = SocketChannel.open();
			sktChannel.configureBlocking(false);
			this.port = port;
			this.host = host;
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
	}
	
	@Override
	public void run() {
		try {
			doConnect();
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
		while (!stop) {
			try {
				selector.select();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectedKeys.iterator();
				SelectionKey  key = null;
				while (iterator.hasNext()) {
					key = iterator.next();
					iterator.remove();
					try {
						handleInput(key);
					} catch (Exception e) {
						if (key!=null) {
							key.cancel();
							if (key.channel()!=null) {
								key.channel().close();
							}
						}
					}
				}
			} catch (IOException e) {
				log.error(e.getMessage(),e);
				System.exit(1);
			}
		}
		if (selector!=null) {
			try {
				selector.close();
			} catch (IOException e) {
				log.error(e.getMessage(),e);
			}
		}
	}

	private void handleInput(SelectionKey key) throws IOException {
		if (key.isValid()) {
			SocketChannel sc = (SocketChannel) key.channel();
			if (key.isConnectable()) {
				if(sc.finishConnect()){
					sc.register(selector, SelectionKey.OP_READ);
					doWrite(sc);
				}else{
					System.exit(1);
				}
			}
			if (key.isReadable()) {
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				int readBytes = sc.read(readBuffer);
				if (readBytes >0) {
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body = new String(bytes,"UTF-8");
					System.out.println("now is :"+body);
					this.stop=true;
				}else if(readBytes<0){
					key.cancel();
					sc.close();
				}
			}
		}
	}

	public void doConnect() throws IOException{
		if(sktChannel.connect(new InetSocketAddress(host, port))){
			sktChannel.register(selector, SelectionKey.OP_READ);
			doWrite(sktChannel);
		}else{
			sktChannel.register(selector, SelectionKey.OP_CONNECT);
		}
	}

	private void doWrite(SocketChannel sktChannel) throws IOException {
		byte[] req = "QUERY TIME ORDER".getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
		writeBuffer.put(req);
		writeBuffer.flip();
		sktChannel.write(writeBuffer);
		if(!writeBuffer.hasRemaining()){
			System.out.println("Send order 2 server succeed!");
		}
	}
	
}
