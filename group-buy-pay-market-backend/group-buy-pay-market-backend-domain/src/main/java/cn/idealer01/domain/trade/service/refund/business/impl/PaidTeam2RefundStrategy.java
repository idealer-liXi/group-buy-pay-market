package cn.idealer01.domain.trade.service.refund.business.impl;

import cn.idealer01.domain.trade.model.aggregate.GroupBuyRefundAggregate;
import cn.idealer01.domain.trade.model.entity.NotifyTaskEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundOrderEntity;
import cn.idealer01.domain.trade.model.valobj.RefundTypeEnumVO;
import cn.idealer01.domain.trade.model.valobj.TeamRefundSuccess;
import cn.idealer01.domain.trade.service.refund.business.AbstractRefundOrderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("paidTeam2RefundStrategy")
public class PaidTeam2RefundStrategy extends AbstractRefundOrderStrategy {

    @Override
    public void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity) {
        log.info("退单，已支付，已完成组团，userId:{}, orderId:{} teamId:{}", tradeRefundOrderEntity.getUserId(), tradeRefundOrderEntity.getOrderId(), tradeRefundOrderEntity.getTeamId());

        // 已成团退单只关闭当前用户订单，不改变已完成的拼团队伍。
        GroupBuyRefundAggregate groupBuyRefundAggregate = GroupBuyRefundAggregate.builder()
                .tradeRefundOrderEntity(tradeRefundOrderEntity)
                .build();
        NotifyTaskEntity notifyTaskEntity = repository.paidTeam2Refund(groupBuyRefundAggregate);

        //2.发送MQ消息
        sendRefundNotifyMessage(notifyTaskEntity, RefundTypeEnumVO.PAID_FORMED.getInfo());

    }

    @Override
    public void reverseStock(TeamRefundSuccess teamRefundSuccess) {
        log.info("退单；已支付、已成团，队伍组队结束，不需要恢复锁单量 {} {} {}", teamRefundSuccess.getUserId(), teamRefundSuccess.getActivityId(), teamRefundSuccess.getTeamId());
    }
}
