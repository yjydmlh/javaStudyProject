package LotteryTicket;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.time.StopWatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Combination {

    private static final Map<Integer, Integer> keyMap = Maps.newHashMap();

    static {
        keyMap.put(MatchResultEnum.WIN.getCode(), MatchResultEnum.DRAW.getCode() + MatchResultEnum.LOSE.getCode());
        keyMap.put(MatchResultEnum.LOSE.getCode(), MatchResultEnum.WIN.getCode() + MatchResultEnum.DRAW.getCode());
        keyMap.put(MatchResultEnum.DRAW.getCode(), MatchResultEnum.WIN.getCode() + MatchResultEnum.LOSE.getCode());

        keyMap.put(MatchResultEnum.DRAW.getCode() + MatchResultEnum.WIN.getCode(), MatchResultEnum.LOSE.getCode());
        keyMap.put(MatchResultEnum.LOSE.getCode() + MatchResultEnum.WIN.getCode(), MatchResultEnum.DRAW.getCode());
        keyMap.put(MatchResultEnum.DRAW.getCode() + MatchResultEnum.LOSE.getCode(), MatchResultEnum.WIN.getCode());
    }

    public static void main(String[] args) {
        List<SportLottery> inSportLotteryList = generateInSportLotteryList();
        List<SportLottery> outSportLotteryList = generateOutSportLotteryList();

        List<List<SportLottery>> inCombinations = getCombinations(inSportLotteryList);
        List<List<SportLottery>> outCombinations = getCombinations(outSportLotteryList);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        System.out.println("内盘组合:");
        for (List<SportLottery> combination : inCombinations) {
            System.out.println(combination);
        }

        System.out.println("外盘组合:");
        for (List<SportLottery> combination : outCombinations) {
            System.out.println(combination);
        }
        List<List<SportLottery>> finalCombinations = getCombinations(inCombinations, outCombinations);
        System.out.println("最终组合：");
        for (List<SportLottery> combination : finalCombinations) {
            System.out.println(combination);
        }

        BigDecimal totalBetAmount = BigDecimal.valueOf(10000);
        BigDecimal inSellRate = BigDecimal.valueOf(0.1);
        List<BetOption> betOptionList = Lists.newArrayList();
        for (List<SportLottery> combination : finalCombinations) {
            BetOption betOption = new BetOption();
            betOption.setMinWinAmount(BigDecimal.ZERO);
            BigDecimal inSellAmount = BigDecimal.ZERO;
            for (SportLottery sportLottery : combination) {
                //当前结果投注额=总投注额/赔率
                BigDecimal betAmount = totalBetAmount.divide(sportLottery.getOdds(), RoundingMode.UP).setScale(0, RoundingMode.UP);
                sportLottery.setBetAmount(betAmount);
                //实际成本
                betOption.setRealCost(betOption.getRealCost().add(sportLottery.getBetAmount()));
                //中奖金额
                sportLottery.setWinAmount(sportLottery.getBetAmount().multiply(sportLottery.getOdds()).setScale(0, RoundingMode.UP));
                if (sportLottery.getInOrOut()) {
                    //内盘体彩出售金额
                    inSellAmount = inSellAmount.add(sportLottery.getBetAmount().multiply(inSellRate));
                    betOption.setInBetAmount(betOption.getInBetAmount().add(sportLottery.getBetAmount()));
                } else {
                    betOption.setOutBetAmount(betOption.getOutBetAmount().add(sportLottery.getBetAmount()));
                }
            }
            //最小中奖金额
            betOption.setMinWinAmount(combination.stream().map(SportLottery::getWinAmount).min(BigDecimal::compareTo).get());

            //体彩出售金额
            betOption.setInSellAmount(inSellAmount);
            betOption.setProfit(betOption.getMinWinAmount().add(betOption.getInSellAmount()).subtract(betOption.getRealCost()));
            betOption.setSportLotteryList(combination);
            if (betOption.getProfit().compareTo(BigDecimal.ZERO) > 0) {
                System.out.println(betOption);
                betOptionList.add(betOption);
            }
        }

        stopWatch.stop();
        System.out.println("耗时：" + stopWatch.getTime());
    }

    private static List<List<SportLottery>> getCombinations(List<List<SportLottery>> inCombinations, List<List<SportLottery>> outCombinations) {

        List<List<SportLottery>> resultList = Lists.newArrayList();

        Map<Integer, List<SportLottery>> inMap = Maps.newHashMap();

        Map<Integer, List<SportLottery>> outMap = Maps.newHashMap();

        System.out.println("内盘");
        for (List<SportLottery> inCombination : inCombinations) {
            int key = 0;
            for (SportLottery inSportLottery : inCombination) {
                key = key + inSportLottery.getMatchResult().getCode();
            }
            inMap.put(key, inCombination);
        }
        System.out.println(inMap);

        System.out.println("外盘");
        for (List<SportLottery> outCombination : outCombinations) {
            int key = 0;
            for (SportLottery inSportLottery : outCombination) {
                key = key + inSportLottery.getMatchResult().getCode();
            }
            outMap.put(key, outCombination);
        }
        System.out.println(inMap);

        inMap.forEach((key, value) -> {
            Integer outKey = keyMap.get(key);
            value.addAll(outMap.get(outKey));
            resultList.add(value);
        });
        return resultList;
    }

    private static List<SportLottery> generateOutSportLotteryList() {
        List<SportLottery> sportLotteries = Lists.newArrayList();
        int[] odds = new int[]{4, 5, 6};
        MatchResultEnum[] matchResultEnums = MatchResultEnum.values();
        for (int i = 0; i < 3; i++) {
            //生成不同的赔率
            SportLottery sportLottery = new SportLottery();
            sportLottery.setOdds(BigDecimal.valueOf(odds[i]));
            sportLottery.setMatchResult(matchResultEnums[i]);
            sportLottery.setInOrOut(false);
            sportLottery.setMatchName("外盘1");
            sportLotteries.add(sportLottery);
        }
        return sportLotteries;
    }

    private static List<SportLottery> generateInSportLotteryList() {
        List<SportLottery> sportLotteries = Lists.newArrayList();
        int[] odds = new int[]{1, 2, 3};
        MatchResultEnum[] matchResultEnums = MatchResultEnum.values();
        for (int i = 0; i < 3; i++) {
            //生成不同的赔率
            SportLottery sportLottery = new SportLottery();
            sportLottery.setOdds(BigDecimal.valueOf(odds[i]));
            sportLottery.setMatchResult(matchResultEnums[i]);
            sportLottery.setInOrOut(true);
            sportLottery.setMatchName("内盘1");
            sportLotteries.add(sportLottery);
        }
        return sportLotteries;
    }

    public static List<List<SportLottery>> getCombinations(List<SportLottery> inSportLotteryList) {
        List<List<SportLottery>> combinations = new ArrayList<>();
        // 添加单元素组合
        for (SportLottery num : inSportLotteryList) {
            List<SportLottery> singleCombination = new ArrayList<>();
            singleCombination.add(num);
            combinations.add(singleCombination);
        }
        // 添加双元素组合
        for (int i = 0; i < inSportLotteryList.size() - 1; i++) {
            for (int j = i + 1; j < inSportLotteryList.size(); j++) {
                List<SportLottery> doubleCombination = new ArrayList<>();
                doubleCombination.add(inSportLotteryList.get(i));
                doubleCombination.add(inSportLotteryList.get(j));
                combinations.add(doubleCombination);
            }
        }
        return combinations;
    }

}
