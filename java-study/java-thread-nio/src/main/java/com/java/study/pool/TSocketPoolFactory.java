package com.java.study.pool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.transport.TSocket;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TSocketPoolFactory extends BasePooledObjectFactory<TSocket> {

    private String host;

    private int    port;

    public TSocketPoolFactory(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public PooledObject<TSocket> wrap(TSocket value) {
        return new DefaultPooledObject<TSocket>(value);
    }

    @Override
    public TSocket create() throws Exception {
        return new TSocket(host, port);
    }

}
