package LotteryTicket;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.time.StopWatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 单关
 * 思路：先找出单场比赛的所有不重复的结果组合，包括内盘和外盘，再用内盘的所有组合去和外盘的组合进行匹配，组成一场比赛的所有投注组合
 * 然后计算每个组合的投注额，取最小的中奖金额，然后计算出所有组合的利润，找出利润为正的所有组合
 */
public class SingleCombinationBet {

    private static final Map<Integer, Integer> resultFlagKeyMap = Maps.newHashMap();

    static {
        resultFlagKeyMap.put(MatchResultEnum.WIN.getCode(), MatchResultEnum.DRAW.getCode() + MatchResultEnum.LOSE.getCode());
        resultFlagKeyMap.put(MatchResultEnum.LOSE.getCode(), MatchResultEnum.WIN.getCode() + MatchResultEnum.DRAW.getCode());
        resultFlagKeyMap.put(MatchResultEnum.DRAW.getCode(), MatchResultEnum.WIN.getCode() + MatchResultEnum.LOSE.getCode());

        resultFlagKeyMap.put(MatchResultEnum.DRAW.getCode() + MatchResultEnum.WIN.getCode(), MatchResultEnum.LOSE.getCode());
        resultFlagKeyMap.put(MatchResultEnum.LOSE.getCode() + MatchResultEnum.WIN.getCode(), MatchResultEnum.DRAW.getCode());
        resultFlagKeyMap.put(MatchResultEnum.DRAW.getCode() + MatchResultEnum.LOSE.getCode(), MatchResultEnum.WIN.getCode());
    }

    public static void main(String[] args) {
        List<SportLottery> inSportLotteryList = generateInSportLotteryList();
        List<SportLottery> outSportLotteryList = generateOutSportLotteryList();

        //获取内盘所有结果组合
        List<List<SportLottery>> inCombinations = getCombinations(inSportLotteryList);
        //获取外盘所有结果组合
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
        //将外盘和内盘所有组合匹配到一起，形成一场比赛的最终投注组合
        List<List<SportLottery>> finalCombinations = getCombinations(inCombinations, outCombinations);
        System.out.println("最终组合：");
        for (List<SportLottery> combination : finalCombinations) {
            System.out.println(combination);
        }

        BigDecimal totalBetAmount = BigDecimal.valueOf(10000);
        BigDecimal inSellRate = BigDecimal.valueOf(0.1);
        List<SingleBetOption> singleBetOptionList = getBetOptionList(finalCombinations, totalBetAmount, inSellRate);

        System.out.println("最佳投注方案：");
        System.out.println(singleBetOptionList);
        stopWatch.stop();
        System.out.println("耗时：" + stopWatch.getTime());
    }

    private static List<SingleBetOption> getBetOptionList(List<List<SportLottery>> finalCombinations, BigDecimal totalBetAmount, BigDecimal inSellRate) {
        List<SingleBetOption> singleBetOptionList = Lists.newArrayList();
        for (List<SportLottery> combination : finalCombinations) {
            SingleBetOption singleBetOption = new SingleBetOption();
            singleBetOption.setMinWinAmount(BigDecimal.ZERO);
            BigDecimal inSellAmount = BigDecimal.ZERO;
            for (SportLottery sportLottery : combination) {
                //当前结果投注额=总投注额/赔率
                BigDecimal betAmount = totalBetAmount.divide(sportLottery.getOdds(), RoundingMode.UP).setScale(0, RoundingMode.UP);
                sportLottery.setBetAmount(betAmount);
                //实际成本
                singleBetOption.setRealCost(singleBetOption.getRealCost().add(sportLottery.getBetAmount()));
                //中奖金额
                sportLottery.setWinAmount(sportLottery.getBetAmount().multiply(sportLottery.getOdds()).setScale(0, RoundingMode.UP));
                if (sportLottery.getInOrOut()) {
                    //内盘体彩出售金额
                    inSellAmount = inSellAmount.add(sportLottery.getBetAmount().multiply(inSellRate));
                    singleBetOption.setInBetAmount(singleBetOption.getInBetAmount().add(sportLottery.getBetAmount()));
                } else {
                    singleBetOption.setOutBetAmount(singleBetOption.getOutBetAmount().add(sportLottery.getBetAmount()));
                }
            }
            //最小中奖金额
            singleBetOption.setMinWinAmount(combination.stream().map(SportLottery::getWinAmount).min(BigDecimal::compareTo).get());

            //体彩出售金额
            singleBetOption.setInSellAmount(inSellAmount);
            singleBetOption.setProfit(singleBetOption.getMinWinAmount().add(singleBetOption.getInSellAmount()).subtract(singleBetOption.getRealCost()));
            singleBetOption.setSportLotteryList(combination);
            if (singleBetOption.getProfit().compareTo(BigDecimal.ZERO) > 0) {
                singleBetOptionList.add(singleBetOption);
            }
        }
        return singleBetOptionList;
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
            Integer outKey = resultFlagKeyMap.get(key);
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
