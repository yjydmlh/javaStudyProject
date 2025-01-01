package LotteryTicket;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 寻找最佳投注组合
 */
public class MultipleCombinationBet {

    private static final Map<String, String> resultFlagMap = Maps.newHashMap();

    public static void main(String[] args) {
        List<MultipleSportLottery> inMatch1 = MultipleBetTest.generateInSportLotteryList1("内盘1");
        List<MultipleSportLottery> inMatch2 = MultipleBetTest.generateInSportLotteryList2("内盘2");

        List<MultipleSportLottery> outMatch1 = MultipleBetTest.generateOutSportLotteryList1("外盘1");
        List<MultipleSportLottery> outMatch2 = MultipleBetTest.generateOutSportLotteryList2("外盘2");

        List<List<MultipleSportLottery>> inMatchList = Lists.newArrayList(inMatch1, inMatch2);
        List<List<MultipleSportLottery>> outMatchList = Lists.newArrayList(outMatch1, outMatch2);

        List<MultipleResult> inResultList = combineAll(inMatchList, true);
        List<MultipleResult> outResultList = combineAll(outMatchList, false);

//        System.out.println("串关内盘结果：" + inResultList);
//        System.out.println("串关外盘结果：" + outResultList);

        System.out.println("\n\n");

        //获取结果组合
        List<List<MultipleResult>> inCombinationList = findCombinations(inResultList, 1, inResultList.size() - 1);
//        List<List<MultipleResult>> outCombinationList = findCombinations(outResultList, 1, outResultList.size() - 1);

//        buildResultFlagMap(inCombinationList, inResultList);

//        System.out.println("串关内盘组合：");
//        System.out.println(inCombinationList);
//        System.out.println("\n");
//        System.out.println("串关外盘组合：");
//        System.out.println(outCombinationList);
//
//        System.out.println("结果标志位映射：");
//        System.out.println(resultFlagMap);

        getFinalCombinationList(inCombinationList, outResultList);

//        System.out.println("最终结果组合:");
//        for (List<MultipleResult> resultList : inCombinationList) {
//            System.out.println(resultList);
//            System.out.println();
//        }

        BigDecimal totalBet = BigDecimal.valueOf(10000);
        BigDecimal inSellRate = BigDecimal.valueOf(0.1);
        List<MultipleBetOption> betOptionList = getBetOptionList(inCombinationList, totalBet, inSellRate);

        System.out.println("可用的投注方案：\n\n");
        for (MultipleBetOption multipleBetOption : betOptionList) {
            System.out.println(multipleBetOption);
            System.out.println("\n\n");
        }
    }

    private static List<List<MultipleResult>> findCombinations(List<MultipleResult> matchResultList, int minSize, int maxSize) {
        List<List<MultipleResult>> combinations = new ArrayList<>();
        for (int i = minSize; i <= maxSize; i++) {
            findCombinations(matchResultList, 0, i, new ArrayList<>(), combinations);
        }
        return combinations;
    }

    private static void findCombinations(List<MultipleResult> matchResultList, int startIndex, int size, List<MultipleResult> currentCombination,
                                         List<List<MultipleResult>> combinations) {
        if (currentCombination.size() == size) {
            combinations.add(new ArrayList<>(currentCombination));
            return;
        }

        for (int i = startIndex; i < matchResultList.size() - (size - currentCombination.size()) + 1; i++) {
            currentCombination.add(matchResultList.get(i));
            findCombinations(matchResultList, i + 1, size, currentCombination, combinations);
            currentCombination.remove(currentCombination.size() - 1);
        }
    }

    private static List<MultipleBetOption> getBetOptionList(List<List<MultipleResult>> finalCombinationList, BigDecimal totalBet, BigDecimal inSellRate) {
        List<MultipleBetOption> betOptionList = Lists.newArrayList();
        for (List<MultipleResult> resultList : finalCombinationList) {
            MultipleBetOption multipleBetOption = new MultipleBetOption();
            multipleBetOption.setTotalBetAmount(totalBet);
            multipleBetOption.setRealCost(BigDecimal.ZERO);
            BigDecimal inBetAmount = BigDecimal.ZERO;
            BigDecimal outBetAmount = BigDecimal.ZERO;
            for (MultipleResult multipleResult : resultList) {
                BigDecimal odds = BigDecimal.ONE;
                for (MultipleSportLottery sportLottery : multipleResult.getResult()) {
                    //串关是多场比赛赔率相乘
                    odds = odds.multiply(sportLottery.getOdds());
                }
                multipleResult.setOdds(odds);
                //当前结果投注额=总投注额/赔率
                BigDecimal betAmount = totalBet.divide(odds, 0, RoundingMode.UP);
                multipleResult.setBetAmount(betAmount);
                //计算内盘投注额和外盘投注额
                if (multipleResult.getInOrOut()) {
                    inBetAmount = inBetAmount.add(multipleResult.getBetAmount());
                } else {
                    outBetAmount = outBetAmount.add(multipleResult.getBetAmount());
                }
                //中奖金额
                multipleResult.setWinAmount(multipleResult.getBetAmount().multiply(odds).setScale(0, RoundingMode.DOWN));
            }
            //计算实际成本
            multipleBetOption.setRealCost(inBetAmount.add(outBetAmount));
            //内盘投注额
            multipleBetOption.setInBetAmount(inBetAmount);
            //外盘投注额
            multipleBetOption.setOutBetAmount(outBetAmount);
            //内盘出售金额
            multipleBetOption.setInSellAmount(inBetAmount.multiply(inSellRate));
            //最小中奖金额
            BigDecimal minWinAmount = resultList.stream().map(MultipleResult::getWinAmount).min(BigDecimal::compareTo).get();
            multipleBetOption.setMinWinAmount(minWinAmount);
            //利润=最小中奖金额+内盘出售金额-实际成本
            multipleBetOption.setProfit(minWinAmount.add(multipleBetOption.getInSellAmount()).subtract(multipleBetOption.getRealCost()));
            multipleBetOption.setMultipleResultList(resultList);
            if (multipleBetOption.getProfit().compareTo(BigDecimal.ZERO) > 0) {
                betOptionList.add(multipleBetOption);
            }
        }
        return betOptionList;
    }

    private static void getFinalCombinationList(List<List<MultipleResult>> inResultList, List<MultipleResult> outResultList) {

        for (List<MultipleResult> inResult : inResultList) {

            List<String> resultFlagList = inResult.stream().map(MultipleResult::getResultFlag).toList();
            for (MultipleResult outResult : outResultList) {
                if (!resultFlagList.contains(outResult.getResultFlag())) {
                    inResult.add(outResult);
                }
            }
        }
    }

    /**
     * 生成串关比赛结果
     *
     * @param arrays
     * @param inOrOut
     * @return
     */
    public static List<MultipleResult> combineAll(List<List<MultipleSportLottery>> arrays, Boolean inOrOut) {
        if (arrays.isEmpty()) {
            return Collections.emptyList();
        }
        List<MultipleResult> multipleResultList = Lists.newArrayList();
        List<List<MultipleSportLottery>> result = new ArrayList<>();
        Queue<List<MultipleSportLottery>> queue = new LinkedList<>();
        queue.add(Collections.emptyList());
        for (List<MultipleSportLottery> candidates : arrays) {
            Queue<List<MultipleSportLottery>> temp = new LinkedList<>();
            while (!queue.isEmpty()) {
                List<MultipleSportLottery> combination = queue.poll();
                for (int i = 0; i < candidates.size(); i++) {
                    List<MultipleSportLottery> newCombination = new ArrayList<>(combination);
                    newCombination.add(candidates.get(i));
                    temp.add(newCombination);
                }
            }
            queue = temp;
        }
        while (!queue.isEmpty()) {
            result.add(queue.poll());
        }
        result.forEach(list -> {
            MultipleResult multipleResult = new MultipleResult();
            multipleResult.setResult(list);
            multipleResult.setInOrOut(inOrOut);
            StringBuilder resultFlag = new StringBuilder();
            int resultFlagInt = 0;
            BigDecimal odds = BigDecimal.ONE;
            for (MultipleSportLottery sportLottery : list) {
                resultFlag.append(sportLottery.getMatchResult().getDesc()).append("_");
                resultFlagInt = resultFlagInt + sportLottery.getMatchResult().getCode();
                odds = odds.multiply(sportLottery.getOdds());
            }
            multipleResult.setResultFlagInt(resultFlagInt);
            multipleResult.setOdds(odds);
            multipleResult.setResultFlag(resultFlag.substring(0, resultFlag.length() - 1));
            multipleResultList.add(multipleResult);
        });
        return multipleResultList;
    }

}
