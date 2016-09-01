package org.java.courses.leaderus;

import java.util.List;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

public class ArrayTableTest {

    public static void main(String[] args) {
        table();
    }

    public static void table(){
        List<Integer> p = Lists.newArrayList();
        p.add(1);
        p.add(2);
        p.add(3);
        List<Integer> rowsTable= Lists.newArrayList(p);
        
        //Create Column Table
        List<String> columnsTables=Lists.newArrayList("First Name","Last Name","Age","sex");
       //ArrayTable is Fix Rows And Columns
        Table<Integer,String,Object> studentTable=ArrayTable.create(rowsTable,columnsTables);
        //Row One
        studentTable.put(1,"First Name",new String("Krisna"));
        studentTable.put(1,"Last Name",new String("Putra"));
        studentTable.put(1,"Age",new Integer(28));
        studentTable.put(1, "sex", "妹子");
        //Row Two
        studentTable.put(2,"First Name",new String("Dira"));
        studentTable.put(2,"Last Name",new String("Safitri"));
        studentTable.put(2,"Age",new Integer(25));
        studentTable.put(2, "sex", "渣男");
        
        System.out.println(studentTable.column("First Name"));
        System.out.println(studentTable.get(2, "Last Name"));
         System.out.println("Google Guava : Arrays Table");
         System.out.println("Select * from Student Table : "+studentTable);
         System.out.println("Select * from Student Table Where Row=1 -->"+studentTable.row(1));
         System.out.println("Select First Name from Student Table where Row=2 -->"+studentTable.get(2,"First Name"));
    }
    
}

class Person{
    
}
