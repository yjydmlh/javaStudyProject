package com.java.study.thread.forkjoinframework.sort;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ForkJoinQuicSort {

    private static class QuickSortTask extends RecursiveAction {

        private int[] array;
        private int low;
        private int high;

        public QuickSortTask(int[] array, int low, int high) {
            this.array = array;
            this.low = low;
            this.high = high;
        }

        @Override
        protected void compute() {
            if (high - low <= 16) {
                // 小于16个元素，使用插入排序
                for (int i = low + 1; i <= high; i++) {
                    int j = i;
                    int temp = array[j];
                    while (j > low && array[j - 1] > temp) {
                        array[j] = array[j - 1];
                        j--;
                    }
                    array[j] = temp;
                }
            } else {
                // 大于16个元素，使用快速排序
                int pivot = array[low + (high - low) / 2];
                int i = low;
                int j = high;
                while (i <= j) {
                    while (array[i] < pivot) {
                        i++;
                    }
                    while (array[j] > pivot) {
                        j--;
                    }
                    if (i <= j) {
                        int temp = array[i];
                        array[i] = array[j];
                        array[j] = temp;
                        i++;
                        j--;
                    }
                }
                if (low < j) {
                    QuickSortTask task1 = new QuickSortTask(array, low, j);
                    task1.fork();
                }
                if (i < high) {
                    QuickSortTask task2 = new QuickSortTask(array, i, high);
                    task2.fork();
                }
            }
        }
    }

    public static int[] generateArray(int size) {
        int[] array = new int[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt();
        }
        return array;
    }

    public static void main(String[] args) {
        int[] array = generateArray(20000000);

        long start = System.currentTimeMillis();
        ForkJoinPool pool = new ForkJoinPool();
        QuickSortTask task = new QuickSortTask(array, 0, array.length - 1);
        pool.invoke(task);
        long end = System.currentTimeMillis();

        System.out.println("forkjoin排序时间：" + (end - start) + "毫秒");

        int[] array2 = generateArray(500000);
        long arrStart = System.currentTimeMillis();
        Arrays.sort(array2);
        System.out.println("jdk自带排序耗时："+(System.currentTimeMillis()-arrStart)+"毫秒");

    }



}
