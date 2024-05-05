package LotteryTicket;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MultipleSportLottery {

    /**
     * 赔率
     */
    private BigDecimal odds;

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

    @Override
    public String toString() {
        return "{ 赔率: " + odds
                +" 比赛结果: "
                + (matchResult != null ? matchResult.getDesc() : "未知")
                + ", 比赛名称: " + matchName + ", 盘口类型: "
                + (inOrOut ? "内盘" : "外盘") + " }\n";
    }


}
