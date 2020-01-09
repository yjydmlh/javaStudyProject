package com.java.study.java8.disruptor;

import java.nio.ByteBuffer;

import com.lmax.disruptor.RingBuffer;

public class MessageEventProducer {

    RingBuffer<MessageEvent> ringBuffer;

    public MessageEventProducer(RingBuffer<MessageEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void onData(ByteBuffer bb) {

    }

}
