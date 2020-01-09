package com.java.study.java8.disruptor;

import com.java.study.java8.protocol.DefaultMessage;
import com.lmax.disruptor.EventTranslatorOneArg;

public class MessageTranslator implements EventTranslatorOneArg<MessageEvent, DefaultMessage> {

    @Override
    public void translateTo(MessageEvent event, long sequence, DefaultMessage arg0) {
        event.setMessage(arg0);
    }

}
