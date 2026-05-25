package cn.idealer01.domain.trade.model.valobj;

import cn.idealer01.types.enums.GroupBuyOrderEnumVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum RefundTypeEnumVO {

    UNPAID_UNLOCK("unpaid_unlock", "未支付，未成团", "unpaid2RefundStrategy"){
        @Override
        public boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO, TradeOrderStatusEnumVO tradeOrderStatusEnumVO) {
            return GroupBuyOrderEnumVO.PROGRESS.equals(groupBuyOrderEnumVO) && TradeOrderStatusEnumVO.CREATE.equals(tradeOrderStatusEnumVO);
        }
    },

    PAID_UNFORMED("paid_unformed", "已支付，未成团", "paid2RefundStrategy"){
        @Override
        public boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO, TradeOrderStatusEnumVO tradeOrderStatusEnumVO) {
            return GroupBuyOrderEnumVO.PROGRESS.equals(groupBuyOrderEnumVO) && TradeOrderStatusEnumVO.COMPLETE.equals(tradeOrderStatusEnumVO);
        }
    },

    PAID_FORMED("paid_formed", "已支付，已成团", "paidTeam2RefundStrategy"){
        @Override
        public boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO, TradeOrderStatusEnumVO tradeOrderStatusEnumVO) {
            return GroupBuyOrderEnumVO.COMPLETE.equals(groupBuyOrderEnumVO) || GroupBuyOrderEnumVO.COMPLETE_FAIL.equals(groupBuyOrderEnumVO)
                    && TradeOrderStatusEnumVO.COMPLETE.equals(tradeOrderStatusEnumVO);
        }
    };


    private String code;
    private String info;
    private String strategy;

    public static RefundTypeEnumVO getRefundTypeEnumVOByCode(String code) {
        switch (code) {
            case "unpaid_unlock":
                return UNPAID_UNLOCK;
            case "paid_unformed":
                return PAID_UNFORMED;
            case "paid_formed":
                return PAID_FORMED;
        }
        throw new RuntimeException("退单类型枚举值不存在: " + code);
    }

    public abstract boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO, TradeOrderStatusEnumVO tradeOrderStatusEnumVO);


    public static RefundTypeEnumVO getRefundStrategy(GroupBuyOrderEnumVO status, TradeOrderStatusEnumVO tradeOrderStatusEnumVO) {
        return Arrays.stream(values())
                .filter(refundTypeEnumVO -> refundTypeEnumVO.matches(status, tradeOrderStatusEnumVO))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到匹配的退单策略 GroupBuyOrderEnumVO=" + status + "TradeOrderStatusEnumVO" + tradeOrderStatusEnumVO));
    }


}
