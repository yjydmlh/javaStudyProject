package com.java.study.thread.forkjoinframework;

import java.util.List;
import java.util.concurrent.RecursiveAction;

public class Task extends RecursiveAction {

    /**
     * 
     */
    private static final long serialVersionUID = -9147591020735551176L;

    private List<Product>     products;

    private int               first;
    private int               last;

    private double            increment;

    public Task(List<Product> produts, int first, int last, double increment) {
        super();
        this.products = produts;
        this.first = first;
        this.last = last;
        this.increment = increment;
    }

    @Override
    protected void compute() {
        if (last - first < 10) {
            updatePrices();
        } else {
            int middle = (last + first) / 2;
            System.out.printf("task: pending tasks:%s\n", getQueuedTaskCount());
            Task t1 = new Task(products, first, middle + 1, increment);
            Task t2 = new Task(products, middle + 1, last, increment);
            invokeAll(t1, t2);
        }
    }

    private void updatePrices() {
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            p.setPrice(p.getPrice() * (i + increment));
        }
    }

}
