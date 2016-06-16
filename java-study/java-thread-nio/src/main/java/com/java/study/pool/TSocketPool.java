package com.java.study.pool;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.thrift.transport.TSocket;

public class TSocketPool {

    private GenericObjectPool<TSocket> pool;

    public TSocketPool(TSocketPoolConfiguration conf) {
        pool = new GenericObjectPool<TSocket>(new TSocketPoolFactory(conf.getHost(), conf.getPort()));
    }

    public TSocket borrow() throws Exception {
        return pool.borrowObject();
    }

    public void returnObject(TSocket tsocket) {
        pool.returnObject(tsocket);
    }

}
