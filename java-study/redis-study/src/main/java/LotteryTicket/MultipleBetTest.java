package LotteryTicket;

import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用于生成彩票数据
 */
public class MultipleBetTest {

    public static void main(String[] args) {

    }

    public static List<MultipleSportLottery> generateOutSportLotteryList1(String matchName) {
        List<MultipleSportLottery> sportLotteries = Lists.newArrayList();
        //生成不同的赔率
        MultipleSportLottery win = new MultipleSportLottery();
        win.setOdds(BigDecimal.valueOf(3.42));
        win.setMatchResult(MatchResultEnum.WIN);
        win.setInOrOut(false);
        win.setMatchName("墨胜利VS墨尔本城FC");

        MultipleSportLottery draw = new MultipleSportLottery();
        draw.setOdds(BigDecimal.valueOf(4.75));
        draw.setMatchResult(MatchResultEnum.DRAW);
        draw.setInOrOut(false);
        draw.setMatchName("墨胜利VS墨尔本城FC");

        MultipleSportLottery lose = new MultipleSportLottery();
        lose.setOdds(BigDecimal.valueOf(3.69));
        lose.setMatchResult(MatchResultEnum.LOSE);
        lose.setInOrOut(false);
        lose.setMatchName("墨胜利VS墨尔本城FC");

        sportLotteries.add(win);
        sportLotteries.add(draw);
        sportLotteries.add(lose);

        return sportLotteries;
    }

    public static List<MultipleSportLottery> generateOutSportLotteryList2(String matchName) {
        List<MultipleSportLottery> sportLotteries = Lists.newArrayList();
        //生成不同的赔率
        MultipleSportLottery win = new MultipleSportLottery();
        win.setOdds(BigDecimal.valueOf(3.42));
        win.setMatchResult(MatchResultEnum.WIN);
        win.setInOrOut(false);
        win.setMatchName("金泉尚武VS仁川联队");

        MultipleSportLottery draw = new MultipleSportLottery();
        draw.setOdds(BigDecimal.valueOf(4.1));
        draw.setMatchResult(MatchResultEnum.LOSE);
        draw.setInOrOut(false);
        draw.setMatchName("金泉尚武VS仁川联队");

        MultipleSportLottery lose = new MultipleSportLottery();
        lose.setOdds(BigDecimal.valueOf(3.88));
        lose.setMatchResult(MatchResultEnum.DRAW);
        lose.setInOrOut(false);
        lose.setMatchName("金泉尚武VS仁川联队");

        sportLotteries.add(win);
        sportLotteries.add(draw);
        sportLotteries.add(lose);

        return sportLotteries;
    }

    public static List<MultipleSportLottery> generateInSportLotteryList1(String matchName) {
        List<MultipleSportLottery> sportLotteries = Lists.newArrayList();
        //生成不同的赔率
        MultipleSportLottery win = new MultipleSportLottery();
        win.setOdds(BigDecimal.valueOf(3.27));
        win.setMatchResult(MatchResultEnum.WIN);
        win.setInOrOut(true);
        win.setMatchName("墨胜利VS墨尔本城FC");

        MultipleSportLottery draw = new MultipleSportLottery();
        draw.setOdds(BigDecimal.valueOf(4.4));
        draw.setMatchResult(MatchResultEnum.DRAW);
        draw.setInOrOut(true);
        draw.setMatchName("墨胜利VS墨尔本城FC");

        MultipleSportLottery lose = new MultipleSportLottery();
        lose.setOdds(BigDecimal.valueOf(3.48));
        lose.setMatchResult(MatchResultEnum.LOSE);
        lose.setInOrOut(true);
        lose.setMatchName("墨胜利VS墨尔本城FC");

        sportLotteries.add(win);
        sportLotteries.add(draw);
        sportLotteries.add(lose);

        return sportLotteries;
    }

    public static List<MultipleSportLottery> generateInSportLotteryList2(String matchName) {
        List<MultipleSportLottery> sportLotteries = Lists.newArrayList();
        //生成不同的赔率
        MultipleSportLottery win = new MultipleSportLottery();
        win.setOdds(BigDecimal.valueOf(3.23));
        win.setMatchResult(MatchResultEnum.WIN);
        win.setInOrOut(true);
        win.setMatchName("金泉尚武VS仁川联队");

        MultipleSportLottery draw = new MultipleSportLottery();
        draw.setOdds(BigDecimal.valueOf(3.9));
        draw.setMatchResult(MatchResultEnum.DRAW);
        draw.setInOrOut(true);
        draw.setMatchName("金泉尚武VS仁川联队");

        MultipleSportLottery lose = new MultipleSportLottery();
        lose.setOdds(BigDecimal.valueOf(3.9));
        lose.setMatchResult(MatchResultEnum.LOSE);
        lose.setInOrOut(true);
        lose.setMatchName("金泉尚武VS仁川联队");

        sportLotteries.add(win);
        sportLotteries.add(draw);
        sportLotteries.add(lose);

        return sportLotteries;
    }

}
