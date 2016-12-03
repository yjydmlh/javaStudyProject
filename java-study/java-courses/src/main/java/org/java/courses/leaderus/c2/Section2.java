package org.java.courses.leaderus.c2;

import sun.misc.Unsafe;

public class Section2 {

	public static void main(String[] args) {
		
	}

}

/**
 * 
 * 用unsface api实现一个高效的多线程共享的数组，其中只有一个线程修改数组，多个线程可以读取数组
 * 
 * @author Administrator
 *
 */
class ArraySafety{
	
	private Unsafe unsafe = Unsafe.getUnsafe();
	private byte[] data;
    private int curPos;//当前有效的数据位置，比如curPos=5，表示从0-5都是有值的，可以读取，追加写入的时候，也从这里开始
    
    public int getCurPos() {
		return curPos;
	}

	public void setCurPos(int curPos) {
		this.curPos = curPos;
	}

	public ArraySafety(int arraySize){
    	data = new byte[arraySize];
    	curPos=-1;
    }
    
    
    
}