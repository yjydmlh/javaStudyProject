package redis;

public class RedisString extends RedisBase{

    public static void main(String[] args) {
        set();
        get();
    }

    public static void set(){
        jedis.set("test","value String");
    }

    public static void get(){
        String value = jedis.get("test");
        System.out.println(value);
    }


}
