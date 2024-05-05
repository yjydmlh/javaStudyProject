package LotteryTicket;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MultipleResult {

    private List<MultipleSportLottery> result;

    /**
     * 内盘还是外盘，true:内盘，false:外盘
     */
    private Boolean inOrOut;

    /**
     * 比赛结果标识
     */
    private String resultFlag;

    /**
     * 赔率
     */
    private BigDecimal odds;

    /**
     * 投注金额
     */
    private BigDecimal betAmount;

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
        return "{" +
                "resultFlag='" + resultFlag +
                ", 盘口类型=" + (inOrOut?"内盘":"外盘") +
                ", 赔率=" + odds +
                ", 投注额=" + betAmount +
                ", 中奖金额=" + winAmount +
                ", result=" + result +
                '}';
    }

}
