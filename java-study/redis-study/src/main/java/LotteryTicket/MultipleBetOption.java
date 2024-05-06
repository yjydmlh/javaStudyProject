package LotteryTicket;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 串关投注方案
 */
@Data
public class MultipleBetOption {

    /**
     * 投注组合
     */
    List<MultipleResult> multipleResultList;

    /**
     * 实际成本,所有结果的投注额之和
     */
    private BigDecimal realCost = BigDecimal.ZERO;

    /**
     * 最小中奖金额
     */
    private BigDecimal minWinAmount;

    /**
     * 内盘投注额
     */
    private BigDecimal inBetAmount = BigDecimal.ZERO;

    /**
     * 外盘投注额
     */
    private BigDecimal outBetAmount = BigDecimal.ZERO;

    /**
     * 内盘出售金额
     */
    private BigDecimal inSellAmount = BigDecimal.ZERO;

    /**
     * 利润
     */
    private BigDecimal profit = BigDecimal.ZERO;

    /**
     * 总投注额
     */
    private BigDecimal totalBetAmount;

    @Override
    public String toString() {
        return "BetOption{" +
                "总投注额=" + totalBetAmount +
                ", 实际成本=" + realCost +
                ", 利润=" + profit +
                ", 最小中奖金额=" + minWinAmount +
                ", 内盘投注额=" + inBetAmount +
                ", 外盘投注额=" + outBetAmount +
                ", 内盘出售金额=" + inSellAmount +
                ",\n 投注方案："+ multipleResultList +
                '}';
    }
}
