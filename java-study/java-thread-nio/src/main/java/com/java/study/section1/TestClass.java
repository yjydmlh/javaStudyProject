package com.java.study.section1;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Random;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import lombok.Getter;
import lombok.Setter;

public class TestClass {

   public static char[] dic = new char[]{'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
    
   public static String path = "e:/myfile/";
   public static String fileName="user.txt";
   
   static Table<Integer, String, Integer> aTable = HashBasedTable.create();
    public static void main(String[] args) throws IOException {
        writeFile();
        System.out.println(aTable.size());
        System.out.println(aTable.row(1));
    }

    public static void readFile() throws IOException{
        File file = new File(path+fileName);
        FileReader reader = new FileReader(file);
        CharBuffer target = CharBuffer.allocate(1024);
        StringBuilder sb = new StringBuilder();
        char[] dst = new char[1024];
        while(reader.read(target)!=-1){
            target.get(dst);
            sb.append(dst);
        }
    }
    
    public static void writeFile() throws IOException{
        File file = new File(path+fileName);
        FileWriter fw = new FileWriter(file);
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i=1;i<=1000000;i++){
            Person p = new Person();
            p.setAge(random.nextInt(18));
            p.setName(getName());
            p.setId(i);
            aTable.put(p.getId(), p.getName(), p.getAge());
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