package org.java.courses.leaderus;

public class Class1 {

    public static void main(String[] args) {
        byteArray();
    }

    /**
     * 定义一个10240*10240的byte数组，分别采用行优先与列优先的循环方式来计算 这些单元格的总和，
     * 看看性能的差距，并解释原因
     * 行优先的做法，每次遍历一行，然后到下一行。
     * 
     * 按列遍历耗时是按行遍历耗时的50倍左右，原因在于java的二维数组存放方式不一样，
     * java的二维数组的存储并不是连续的内存空间，一维数组的每个元素都是指向了一个一维数组，
     * 按行遍历的时候是遍历一个一个一维数组，寻址下一个数组的次数是10240次。
     * 按列遍历的时候是每个元素都要去寻址下一个数组，一共的寻址次数是10240*10240次
     * 按列遍历的每个元素都在不连续的内存区域中，所以比较慢
     * 
     * 
     */
    public static void byteArray() {
        byte[][] array = new byte[10240][10240];
        init(array);
        long start1 = System.currentTimeMillis();
        sumLine(array);
        System.out.println("行遍历求和 耗时:"+(System.currentTimeMillis() - start1)+"毫秒");
        long start2 = System.currentTimeMillis();
        sumColumn(array);
        System.out.println("列遍历求和 耗时:"+(System.currentTimeMillis() - start2)+"毫秒");
    }

    private static void sumColumn(byte[][] array) {
        int sum=0;
        for(int i=0;i<array.length;i++){
            for(int j=0;j<array.length;j++){
                sum=sum+array[j][i];
            }
        }
        System.out.println("数组元素和："+sum);
    }

    private static void sumLine(byte[][] array) {
        int sum=0;
        for(int i=0;i<array.length;i++){
            for(int j=0;j<array.length;j++){
                sum=sum+array[i][j];
            }
        }
        System.out.println("数组元素和："+sum);
    }

    private static void init(byte[][] array) {
        for(int i=0;i<array.length;i++){
            for(int j=0;j<array.length;j++){
                array[i][j]=1;
            }
        }
    }

}
