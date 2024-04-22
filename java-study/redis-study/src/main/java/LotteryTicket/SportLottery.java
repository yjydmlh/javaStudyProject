package LotteryTicket;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SportLottery {

    /**
     * 赔率
     */
    private BigDecimal odds;

    /**
     * 投注金额
     */
    private BigDecimal betAmount;

    /**
     * 比赛结果
     */
    private MatchResultEnum matchResult;

    /**
     * 比赛名称
     */
    private String matchName;

    /**
     * true:内盘,false:外盘
     */
    private Boolean inOrOut;

    /**
     * 最小投注额
     */
    private BigDecimal minBetAmount;

    /**
     * 中奖金额
     */
    private BigDecimal winAmount;

    /**
     * 获取中奖金额
     * @return 中奖金额
     */
    public BigDecimal getWinAmount() {
        return betAmount.multiply(odds);
    }

    @Override
    public String toString() {
        return "{ 赔率: " + odds + ", 投注金额: " + (betAmount != null ? betAmount : "未填写") + ", 中奖金额: "+winAmount+" 比赛结果: " + (matchResult != null ? matchResult.getDesc() : "未知") + ", 比赛名称: " + matchName + ", 盘口类型: " + (inOrOut ? "内盘" : "外盘") + " }";
    }


}
