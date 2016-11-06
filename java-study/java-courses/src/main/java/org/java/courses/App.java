package org.java.courses;

import sun.misc.Unsafe;

/**
 * Hello world!
 *
 */
public class App 
{
    @SuppressWarnings("restriction")
	public static void main( String[] args )
    {
    	Unsafe.getUnsafe().loadFence();
    	Unsafe.getUnsafe().storeFence();
    	Unsafe.getUnsafe().fullFence();
        System.out.println( "Hello World!" );
        
    }
}
