package com.java.study.thread.forkjoinframework.product;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class Test {

    public static void main(String[] args) {
        ProductListGenerator pg = new ProductListGenerator();
        List<Product> products = pg.generate(100);

        Task task = new Task(products, 0, products.size(), 0.01);

        ForkJoinPool pool = new ForkJoinPool();
        pool.execute(task);

        do {
            System.out.printf("Main: Thread Count: %d\n", pool.getActiveThreadCount());
            System.out.printf("Main: Thread Steal: %d\n", pool.getStealCount());
            System.out.printf("Main: Parallelism: %d\n", pool.getParallelism());
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!task.isDone());
        pool.shutdown();

        if (task.isCompletedNormally()) {
            System.out.println("main:The process has completed normally.");
        }

        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            if (product.getPrice() != 12) {
                System.out.printf("product %s:%f\n", product.getName(), product.getPrice());
            }
        }
        System.out.println("main:end of the program.");
    }

}
