package org.java.courses.leaderus.c7;

import java.util.AbstractList;

public class UnsafeArrayList<E>  extends AbstractList<E>{

	private Object[] elementData;
	
	private static final int DEFAULT_SIZE=4;
	
	private int capacity=DEFAULT_SIZE;
	
	public UnsafeArrayList(){
		this(DEFAULT_SIZE);
	}
	
	public UnsafeArrayList(int capacity){
		elementData = new Object[capacity];
		this.capacity = capacity;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E get(int index) {
		if(index >= this.capacity){
			throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
		}
		E e = (E)elementData[index];
		return e;
	}

	@Override
	public boolean add(E e) {
		
		return super.add(e);
	}
	
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+this.capacity;
    }
	
	@Override
	public int size() {
		return this.capacity;
	}
	
}
