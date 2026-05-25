package cn.idealer01.domain.trade.service.refund.factory;

import cn.idealer.wrench.design.framework.link.model2.DynamicContext;
import cn.idealer.wrench.design.framework.link.model2.LinkArmory;
import cn.idealer.wrench.design.framework.link.model2.chain.BusinessLinkedList;
import cn.idealer01.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.idealer01.domain.trade.model.entity.MarketPayOrderEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundBehaviorEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundCommandEntity;
import cn.idealer01.domain.trade.service.refund.filter.DataNodeFilter;
import cn.idealer01.domain.trade.service.refund.filter.RefundNodeFilter;
import cn.idealer01.domain.trade.service.refund.filter.UniqueRefundNodeFilter;
import lombok.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class TradeRefundRuleFilterFactory {

    @Bean("tradeRefundRuleFilter")
    public BusinessLinkedList<TradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> tradeRefundRuleFilter(
            DataNodeFilter dataNodeFilter,
            UniqueRefundNodeFilter uniqueRefundNodeFilter,
            RefundNodeFilter refundNodeFilter
    ){
        LinkArmory<TradeRefundCommandEntity, DynamicContext, TradeRefundBehaviorEntity> armory =
                new LinkArmory<>("退单规则过滤链",
                        dataNodeFilter,
                        uniqueRefundNodeFilter,
                        refundNodeFilter);

        return armory.getLogicLink();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext extends cn.idealer.wrench.design.framework.link.model2.DynamicContext {
        private MarketPayOrderEntity marketPayOrderEntity;
        private GroupBuyTeamEntity groupBuyTeamEntity;

    }


}
