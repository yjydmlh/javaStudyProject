package com.java.study.java8.disruptor;

import java.nio.ByteBuffer;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

public class DisruptorTest {

    public static void main(String[] args) throws InterruptedException {
        MessageEventFactory eventFactory = new MessageEventFactory();
        int ringBufferSize = 1024 * 1024; // RingBuffer 大小，必须是 2 的 N 次方；

        Disruptor<MessageEvent> disruptor = new Disruptor<MessageEvent>(eventFactory, ringBufferSize,
                new MessageThreadFactory(), ProducerType.MULTI, new YieldingWaitStrategy());
        disruptor.setDefaultExceptionHandler(new MessageExceptionHandler());
        MessageEventHandler eventHandler = new MessageEventHandler();
        disruptor.handleEventsWith(eventHandler);
        disruptor.start();

        RingBuffer<MessageEvent> ringBuffer = disruptor.getRingBuffer();

        MessageEventProducer producer = new MessageEventProducer(ringBuffer);

        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; true; l++) {
            bb.putLong(0, l);
            producer.onData(bb);
            Thread.sleep(1000);
        }
    }

}
