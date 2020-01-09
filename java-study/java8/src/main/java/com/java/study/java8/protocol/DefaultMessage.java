package com.java.study.java8.protocol;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public final class DefaultMessage {

	private Header header;

	private Object body;

}
