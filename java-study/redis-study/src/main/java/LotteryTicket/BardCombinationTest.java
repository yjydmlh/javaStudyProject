package LotteryTicket;

import cn.hutool.core.date.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BardCombinationTest {

    public static void main(String[] args) {
        int numElements = 9;
        String[] arr = new String[numElements];
        for (int i = 0; i < numElements; i++) {
            arr[i] = ("a" + i);
        }
        int minSize = 1; // 最小组合元素个数
        int maxSize = arr.length - 1; // 最大组合元素个数
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        findCombinations(arr, minSize, maxSize);
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }

    private static void findCombinations(String[] arr, int minSize, int maxSize) {
        List<List<String>> combinations = new ArrayList<>();
        for (int i = minSize; i <= maxSize; i++) {
            findCombinations(arr, 0, i, new ArrayList<>(), combinations);
        }

        System.out.println("所有组合：");
        for (List<String> combination : combinations) {
            System.out.println(combination);
        }
    }

    private static void findCombinations(String[] arr, int startIndex, int size, List<String> currentCombination,
                                         List<List<String>> combinations) {
        if (currentCombination.size() == size) {
            combinations.add(new ArrayList<>(currentCombination));
            return;
        }

        for (int i = startIndex; i < arr.length - (size - currentCombination.size()) + 1; i++) {
            currentCombination.add(arr[i]);
            findCombinations(arr, i + 1, size, currentCombination, combinations);
            currentCombination.remove(currentCombination.size() - 1);
        }
    }

}
