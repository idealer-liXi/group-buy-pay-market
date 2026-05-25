package cn.idealer01.domain.trade.service.refund.filter;

import cn.idealer.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.idealer01.domain.trade.model.entity.MarketPayOrderEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundBehaviorEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundCommandEntity;
import cn.idealer01.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import cn.idealer01.domain.trade.service.refund.factory.TradeRefundRuleFilterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UniqueRefundNodeFilter implements ILogicHandler<TradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity>{
    @Override
    public TradeRefundBehaviorEntity apply(TradeRefundCommandEntity tradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("逆向流程-退单操作，重复退单检查 userId:{} outTradeNo:{}", tradeRefundCommandEntity.getUserId(), tradeRefundCommandEntity.getOutTradeNo());

        MarketPayOrderEntity marketPayOrderEntity = dynamicContext.getMarketPayOrderEntity();
        TradeOrderStatusEnumVO status = marketPayOrderEntity.getTradeOrderStatusEnumVO();

        //已经完成退单
        if(status.equals(TradeOrderStatusEnumVO.CLOSE)){
            return TradeRefundBehaviorEntity.builder()
                    .userId(tradeRefundCommandEntity.getUserId())
                    .orderId(marketPayOrderEntity.getOrderId())
                    .teamId(marketPayOrderEntity.getTeamId())
                    .tradeRefundBehaviorEnum(TradeRefundBehaviorEntity.TradeRefundBehaviorEnum.REPEAT)
                    .build();
        }

        return next(tradeRefundCommandEntity, dynamicContext);
    }
}
