package com.java.study.pool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TSocketPoolConfiguration extends GenericObjectPoolConfig {

    private String host;

    private int    port;

}
