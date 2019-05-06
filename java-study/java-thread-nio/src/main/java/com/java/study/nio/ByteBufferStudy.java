package com.java.study.nio;

import java.nio.Buffer;
import java.nio.ByteBuffer;


public class ByteBufferStudy {

    public static void main(String[] args) {
        byteBuffer();
    }

    public static void  byteBuffer(){
        ByteBuffer btBuf = ByteBuffer.allocate(1024);
        btBuf.put((byte)'a');
        btBuf.put((byte)'b');
        btBuf.put((byte)'c');
        printBufferInfo(btBuf);
        System.out.println(btBuf.getChar(1));
        btBuf.flip();
        printBufferInfo(btBuf);
        btBuf.reset();
        printBufferInfo(btBuf);
        btBuf.rewind();
        printBufferInfo(btBuf);
    }
    
    public static void printBufferInfo(Buffer buffer){
        System.out.println("position:"+buffer.position());
        System.out.println("capacity:"+buffer.capacity());
        System.out.println("limit:"+buffer.limit());
        System.out.println("mark:"+buffer.mark());
    }
    
}
