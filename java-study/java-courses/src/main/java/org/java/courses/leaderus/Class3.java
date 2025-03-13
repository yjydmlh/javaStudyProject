//package org.java.courses.leaderus;
//
//import javolution.util.FastSortedTable;
//import javolution.util.FastTable;
//
//public class Class3 {
//
//    public static void main(String[] args) {
//        // TODO Auto-generated method stub
//    	FastSortedTable<String> t = new FastSortedTable<>();
//    	t.add("3qwr");
//    	t.add("3fwr");
//    	t.add("3hwr");
//    	t.sort();
//    	System.out.println(t);
//    }
//
//    /**
//     * 编码实现下面的要求
//     *  现有对象 MyItem {byte type,byte color,byte price} ，
//     *  要求将其内容存放在一个扁平的byte[]数组存储数据的ByteStore {byte[] storeByteArry}对象里,
//     * 即每个MyItem占用3个字节，第一个MyItem占用storeByteArry[0]-storeByteArry[2] 3个连续字节，
//     * 以此类推，最多能存放1000个MyItem对象
//     * ByteStore提供如下方法
//     * putMyItem(int index,MyItem item) 在指定的Index上存放MyItem的属性，这里的Index是0-999，而不是storeByteArry的Index
//     * getMyItem(int index),从指定的Index上查找MyItem的属性，并返回对应的MyItem对象。
//     *
//     *要求放入3个MyItem对象（index为0-2）并比较getMyItem方法返回的这些对象是否与之前放入的对象equal。
//       加分功能如下：
//        放入1000个MyItem对象到ByteStore中，采用某种算法对storeByteArry做排序，
//        排序以price为基准，排序完成后，输出前100个结果
//     *
//     *
//     *
//     *
//     */
//    public static void test(){
//
//    }
//
//}
