package redis;

import com.ip2location.IP2Location;
import com.ip2location.IPResult;
import redis.clients.jedis.Jedis;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        IP2Location loc = new IP2Location();
        loc.Open("F:\\myproject\\javaStudyProject\\javaStudyProject\\java-study\\redis-study\\src\\main\\java\\redis\\IP2LOCATION-LITE-DB11.BIN");
//        loc.open("IP2LOCATION.BIN");
//        loc.
        IPResult result = loc.IPQuery("8.218.180.119");
//        IPResult result = loc.IPQuery("175.176.37.139");
        System.out.println(result);
    }
}