package com.java.study.java8.disruptor;

import com.java.study.java8.protocol.DefaultMessage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MessageEvent {

    private DefaultMessage message;
}
