package cn.idealer01.domain.trade.service.refund.filter;

import cn.idealer.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.idealer01.domain.trade.model.entity.*;
import cn.idealer01.domain.trade.model.valobj.RefundTypeEnumVO;
import cn.idealer01.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import cn.idealer01.domain.trade.service.refund.business.IRefundOrderStrategy;
import cn.idealer01.domain.trade.service.refund.factory.TradeRefundRuleFilterFactory;
import cn.idealer01.types.enums.GroupBuyOrderEnumVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Service
public class RefundNodeFilter implements ILogicHandler<TradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> {
    @Resource
    private Map<String, IRefundOrderStrategy> refundOrderStrategyMap;
    @Override
    public TradeRefundBehaviorEntity apply(TradeRefundCommandEntity tradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        //1.获取上下文数据
        MarketPayOrderEntity marketPayOrderEntity = dynamicContext.getMarketPayOrderEntity();
        GroupBuyTeamEntity groupBuyTeamEntity = dynamicContext.getGroupBuyTeamEntity();

        TradeOrderStatusEnumVO tradeOrderStatusEnumVO = marketPayOrderEntity.getTradeOrderStatusEnumVO();
        GroupBuyOrderEnumVO status = groupBuyTeamEntity.getStatus();

        //2.获得退单策略
        RefundTypeEnumVO refundTypeEnumVO = RefundTypeEnumVO.getRefundStrategy(status, tradeOrderStatusEnumVO);
        IRefundOrderStrategy refundOrderStrategy = refundOrderStrategyMap.get(refundTypeEnumVO.getStrategy());

        //3.执行退单操作
        refundOrderStrategy.refundOrder(TradeRefundOrderEntity.builder()
                        .userId(tradeRefundCommandEntity.getUserId())
                        .orderId(marketPayOrderEntity.getOrderId())
                        .teamId(marketPayOrderEntity.getTeamId())
                        .activityId(groupBuyTeamEntity.getActivityId())
                        .outTradeNo(tradeRefundCommandEntity.getOutTradeNo())
                .build());

        return TradeRefundBehaviorEntity.builder()
                .userId(tradeRefundCommandEntity.getUserId())
                .orderId(marketPayOrderEntity.getOrderId())
                .teamId(marketPayOrderEntity.getTeamId())
                .tradeRefundBehaviorEnum(TradeRefundBehaviorEntity.TradeRefundBehaviorEnum.SUCCESS)
                .build();
    }
}
