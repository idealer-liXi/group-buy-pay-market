package cn.idealer01.domain.trade.model.aggregate;

import cn.idealer01.domain.trade.model.entity.TradeRefundOrderEntity;
import cn.idealer01.domain.trade.model.valobj.GroupBuyProgressVO;
import cn.idealer01.types.enums.GroupBuyOrderEnumVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupBuyRefundAggregate {

    //退单实体
    private TradeRefundOrderEntity tradeRefundOrderEntity;

    //退单进度
    private GroupBuyProgressVO groupBuyProgressVO;

    //拼团状态
    private GroupBuyOrderEnumVO groupBuyOrderEnumVO;

    public static GroupBuyRefundAggregate buildUnpaid2RefundAggregate(TradeRefundOrderEntity tradeRefundOrderEntity, Integer lockCount) {
        return GroupBuyRefundAggregate.builder()
                .tradeRefundOrderEntity(tradeRefundOrderEntity)
                .groupBuyProgressVO(GroupBuyProgressVO.builder()
                        .lockCount(lockCount)
                        .build())
                .build();
    }

    public static GroupBuyRefundAggregate buildPaid2RefundAggregate(TradeRefundOrderEntity tradeRefundOrderEntity, Integer lockCount, Integer completeCount) {
        return GroupBuyRefundAggregate.builder()
                .tradeRefundOrderEntity(tradeRefundOrderEntity)
                .groupBuyProgressVO(GroupBuyProgressVO.builder()
                        .completeCount(completeCount)
                        .lockCount(lockCount)
                        .build())
                .build();
    }

    public static GroupBuyRefundAggregate buildPaidTeam2RefundAggregate(TradeRefundOrderEntity tradeRefundOrderEntity, Integer lockCount, Integer completeCount, GroupBuyOrderEnumVO groupBuyOrderEnumVO) {
        return GroupBuyRefundAggregate.builder()
                .tradeRefundOrderEntity(tradeRefundOrderEntity)
                .groupBuyProgressVO(GroupBuyProgressVO.builder()
                        .lockCount(lockCount)
                        .completeCount(completeCount)
                        .build())
                .groupBuyOrderEnumVO(groupBuyOrderEnumVO)
                .build();
    }
}
