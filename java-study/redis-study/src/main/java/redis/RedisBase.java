package redis;

import redis.clients.jedis.Jedis;

public class RedisBase {

    public final static Jedis jedis = new Jedis("192.168.26.136",7379);
    static {
        jedis.auth("lilishop");
    }

}
