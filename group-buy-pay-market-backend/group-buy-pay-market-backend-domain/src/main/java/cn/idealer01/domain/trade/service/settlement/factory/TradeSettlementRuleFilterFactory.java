package cn.idealer01.domain.trade.service.settlement.factory;

import cn.idealer.wrench.design.framework.link.model2.DynamicContext;
import cn.idealer.wrench.design.framework.link.model2.LinkArmory;
import cn.idealer.wrench.design.framework.link.model2.chain.BusinessLinkedList;
import cn.idealer01.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.idealer01.domain.trade.model.entity.MarketPayOrderEntity;
import cn.idealer01.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import cn.idealer01.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import cn.idealer01.domain.trade.service.settlement.filter.EndRuleFilter;
import cn.idealer01.domain.trade.service.settlement.filter.OutTradeNoRuleFilter;
import cn.idealer01.domain.trade.service.settlement.filter.SCRuleFilter;
import cn.idealer01.domain.trade.service.settlement.filter.SettableRuleFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TradeSettlementRuleFilterFactory {

    @Bean("tradeSettlementRuleFilter")
    public BusinessLinkedList<TradeSettlementRuleCommandEntity, DynamicContext, TradeSettlementRuleFilterBackEntity> tradeSettlementRuleFilter(
           SCRuleFilter scRuleFilter,
           OutTradeNoRuleFilter outTradeNoRuleFilter,
           SettableRuleFilter settableRuleFilter,
           EndRuleFilter endRuleFilter
    ){

        LinkArmory<TradeSettlementRuleCommandEntity, DynamicContext, TradeSettlementRuleFilterBackEntity> linkArmory = new LinkArmory<>("交易结算规则过滤连", scRuleFilter, outTradeNoRuleFilter, settableRuleFilter, endRuleFilter);

        return linkArmory.getLogicLink();
    }



    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DynamicContext extends cn.idealer.wrench.design.framework.link.model2.DynamicContext {
        // 订单营销实体对象
        private MarketPayOrderEntity marketPayOrderEntity;
        // 拼团组队实体对象
        private GroupBuyTeamEntity groupBuyTeamEntity;
    }

}
