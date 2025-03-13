package org.java.courses.leaderus;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.CharBuffer;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import lombok.Getter;
import lombok.Setter;

//import sun.misc.Unsafe;

public class Courses12 {

	 public static char[] dic = new char[]{'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
	   
	   public static String path = "e:/";
	   public static String fileName="user.txt";
	   
	   static Table<Integer, String, Integer> aTable = HashBasedTable.create();
	   static Table<Integer, String, Integer> bTable = HashBasedTable.create();
	   
	    public static void main(String[] args) throws IOException {
//	        writeFile();
	    	readFile();
	    	readFile2();
	        System.out.println(aTable.size());
	        System.out.println(bTable.size());
	        System.out.println(aTable.row(1));
	        System.out.println(filterByAge(aTable,6,11));
	    }

	    public static long filterByAge(Table<Integer, String, Integer> aTable,int startAge,int endAge){
	    	long p = aTable.cellSet().parallelStream().filter(new Predicate<Cell<Integer, String, Integer>> () {
				@Override
				public boolean test(Cell<Integer, String, Integer> t) {
					if(startAge<=t.getValue() && endAge>=t.getValue()){
						return true;
					}
					return false;
				}
			}).count();
	    	return p;
	    } 
	    
	    public static void readFile2() throws IOException{
	        File file = new File(path+fileName);
	        FileReader reader = new FileReader(file);
	        LineNumberReader read = new LineNumberReader(reader);
	        String line = read.readLine();
	        while(line != null){
	        	String[] d = line.trim().split(",");
	        	bTable.put(Integer.valueOf(d[0]),d[1] , Integer.valueOf(d[2]));
	        	line = read.readLine();
	        }
	    }
	    
	    public static void readFile() throws IOException{
	        File file = new File(path+fileName);
	        FileReader reader = new FileReader(file);
	        LineNumberReader read = new LineNumberReader(reader);
	        read.lines().parallel().forEach(new Consumer<String>() {
				@Override
				public void accept(String t) {
					String[] d = t.trim().split(",");
					aTable.put(Integer.valueOf(d[0]),d[1] , Integer.valueOf(d[2]));
				}
			});
	    }
	    
	    public static void writeFile() throws IOException{
	        File file = new File(path+fileName);
	        if(file.exists()){
	        	file.delete();
	        }
	        file.createNewFile();
	        FileWriter fw = new FileWriter(file);
	        Random random = new Random();
	        StringBuilder sb = new StringBuilder();
	        for(int i=1;i<=1000000;i++){
	            Person p = new Person();
	            p.setAge(random.nextInt(18));
	            p.setName(getName());
	            p.setId(i);
	            sb.append(p.toString()+"\n");
	            if(i%500000 == 0){
	                fw.write(sb.toString());
	                sb.delete(0, sb.length());
	            }
	        }
	        fw.flush();
	        fw.close();
	    }
	    
	    public static String getName(){
	        Random rand = new Random();
	        StringBuilder sb = new StringBuilder();
	        for(int i=0;i<8;i++){
	            sb.append(dic[rand.nextInt(dic.length)]);
	        }
	        return sb.toString();
	    }

}
@Getter
@Setter
class Person{
    private Integer id;
    private String name;
    private Integer age;
    
    @Override
    public String toString(){
        return  id+","+name+","+age;
    }
}