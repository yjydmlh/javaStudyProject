package com.java.study.java8.protocol;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public final class Header {

	private int crcCode = 0xabef0101;

	private int length;

	private long sessionID;

	private byte type;

	private byte priority;

	private Map<String, Object> attachment = new HashMap<String, Object>();
}
