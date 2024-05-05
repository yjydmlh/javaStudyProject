package LotteryTicket;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum MatchResultEnum {

    WIN(8,"win"),
    LOSE(4,"lose"),
    DRAW(1,"draw");

    private final Integer code;
    private final String desc;

    MatchResultEnum(Integer code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public static void main(String[] args) {
        List<MatchResultEnum> list = Arrays.asList(MatchResultEnum.values());
    }

}
