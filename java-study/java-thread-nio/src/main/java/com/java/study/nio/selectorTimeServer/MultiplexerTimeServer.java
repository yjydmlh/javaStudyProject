package com.java.study.nio.selectorTimeServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.joda.time.DateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class MultiplexerTimeServer  implements Runnable{

	
	private Selector selector;
	private ServerSocketChannel servChannel;
	private volatile boolean stop=false;
	
	
	public MultiplexerTimeServer(int port) {
		
		try {
			selector = Selector.open();
			servChannel = ServerSocketChannel.open();
			servChannel.configureBlocking(false);
			servChannel.bind(new InetSocketAddress(port));
			servChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("the time server is start in port :"+port);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			System.exit(1);
		}
	}
	
	public void stop(){
		this.stop = true;
	}
	
	@Override
	public void run() {
		while(!stop){
			try {
				selector.select();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> selKeyIterator = selectedKeys.iterator();
				SelectionKey key = null;
				while (selKeyIterator.hasNext()) {
					key = selKeyIterator.next();
					selKeyIterator.remove();
					try {
						handleInput(key);
					} catch (IOException e) {
						log.error(e.getMessage(),e);
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		if(selector != null){
			try {
				selector.close();
			} catch (IOException e) {
				log.error(e.getMessage(),e);
			}
		}
	}

	private void handleInput(SelectionKey key) throws IOException {
		if (key.isValid()) {
			if(key.isAcceptable()){
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				sc.register(selector, SelectionKey.OP_READ);
			}
			if (key.isReadable()) {
				SocketChannel sc = (SocketChannel) key.channel();
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				int readBytes = sc.read(readBuffer);
				if (readBytes >0 ) {
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body = new String(bytes,"UTF-8");
					System.out.println("the time server receive order:"+body);
					String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? DateTime.now().toString():"BAD ORDER";
					doWrite(sc,currentTime);
				}
			}
		}
	}

	private void doWrite(SocketChannel sc, String response) throws IOException {
		if (response != null && response.trim().length()>0) {
			byte[] bytes = response.getBytes();
			ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
			writeBuffer.put(bytes);
			writeBuffer.flip();
			sc.write(writeBuffer);
		}
	}

}
