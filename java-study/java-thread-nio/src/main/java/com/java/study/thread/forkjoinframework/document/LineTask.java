package com.java.study.thread.forkjoinframework.document;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;

public class LineTask extends RecursiveTask<Integer> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String            line[];

    private int               start;
    private int               end;
    private String            word;

    public LineTask(String line[], int start, int end, String word) {
        this.line = line;
        this.start = start;
        this.end = end;
        this.word = word;
    }

    @Override
    protected Integer compute() {
        Integer result = null;
        if (end - start < 100) {
            result = count(line, start, end, word);
        } else {
            int mid = (start + end) / 2;
            LineTask task1 = new LineTask(line, start, mid, word);
            LineTask task2 = new LineTask(line, mid, end, word);
            invokeAll(task1, task2);
            try {
                result = groupResults(task1.get(), task2.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private Integer count(String[] line2, int start2, int end2, String word2) {
        int count = 0;
        for (int i = start2; i < end2; i++) {
            if (line2[i].equals(word2)) {
                count++;
            }
        }
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return count;
    }

    private Integer groupResults(Integer integer, Integer integer2) {
        return integer + integer2;
    }

}
