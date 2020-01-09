package com.java.study.java8.disruptor;

import java.util.concurrent.ThreadFactory;

public class MessageThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r);
    }

}
