//package org.java.courses.leaderus;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Random;
//
////import jdk.internal.vm.annotation.Contended;
//import lombok.Getter;
//import lombok.Setter;
//import lombok.ToString;
//
///**
// * 定义Java类Salary {String name, int baseSalary, int bonus
// * },随机产生1万个实例，属性也随机产生（baseSalary范围是5-100万，
// * bonus为（0-10万），其中name长度为5，随机字符串，然后进行排序，
// * 排序方式为收入总和（baseSalary*13+bonus），
// * 输出收入最高的10个人的名单
// */
//public class Class2 {
//
//    public static void main(String[] args) {
//        generate();
////        System.out.println(randomStr(5));
//    }
//
//    public static void generate(){
//        List<Salary>  l = new ArrayList<Salary>();
//        Salary s = null;
//        for(int i=0;i<10000;i++){
//            s = new Salary();
//            s.setBonus(random(0,100000));
//            s.setBaseSalary(random(5,1000000));
//            s.setName(randomStr(5));
//            l.add(s);
//        }
//        l.sort(new Comparator<Salary>() {
//
//            @Override
//            public int compare(Salary o1, Salary o2) {
//                int s1 = o1.getBaseSalary()*13+o1.getBonus();
//                int s2 = o2.getBaseSalary()*13+o2.getBonus();
//                if(s1==s2){
//                    return 0;
//                }else if(s1>s2){
//                    return -1;
//                }
//                return 1;
//            }
//        });
//        for(int i=0;i<10;i++){
//        	System.out.println(l.get(i));
//        }
//    }
//
//
//    public static String randomStr(int n) {
//        StringBuffer sb = new StringBuffer();
//        for(int i=0;i<n;i++){
//            int t = random(97,122);
//            sb.append((char)t);
//        }
//        return sb.toString();
//    }
//
//    public static int random(int down,int up){
//        Random rand = new Random();
//        int r = rand.nextInt(up);
//        if(r>=down){
//            return r;
//        }
//        return r+down > up ?up:r+down;
//    }
//
//}
//@Getter
//@Setter
////@Contended
//class Salary{
//    private String name;
//    private int bonus;
//    private int baseSalary;
//
//    @Override
//    public String toString(){
//        return  "Salary(name="+name+",bonus="+bonus+",baseSalary="+baseSalary+",sum="+(baseSalary*13+bonus)+")";
//    }
//
//}