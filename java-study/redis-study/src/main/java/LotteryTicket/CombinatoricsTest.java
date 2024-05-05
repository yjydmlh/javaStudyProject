package LotteryTicket;

import org.apache.commons.lang3.time.StopWatch;
import org.paukov.combinatorics3.Generator;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CombinatoricsTest {

    public static void main(String[] args) {
        int numElements = 9;
        String[] elements = new String[numElements];
        for (int i = 0; i < numElements; i++) {
            elements[i] = ("a" + i);
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<List<String>> rs = Generator.subset(elements).simple().stream().collect(Collectors.toList());
        stopWatch.stop();
        System.out.println("Subset: " + stopWatch.getTime(TimeUnit.MILLISECONDS));
    }

}
