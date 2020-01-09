package com.java.study.java8.disruptor;

import com.lmax.disruptor.EventHandler;

public class MessageEventHandler implements EventHandler<MessageEvent> {

    @Override
    public void onEvent(MessageEvent event, long sequence, boolean endOfBatch) throws Exception {
        System.out.println("收到消息：" + event.getMessage() + ",sequence=" + sequence);
    }

}
