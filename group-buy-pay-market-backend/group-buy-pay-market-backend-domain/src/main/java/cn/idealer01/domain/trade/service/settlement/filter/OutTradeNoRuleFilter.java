package cn.idealer01.domain.trade.service.settlement.filter;

import cn.idealer01.domain.trade.adapter.respository.ITradeRepository;
import cn.idealer01.domain.trade.model.entity.MarketPayOrderEntity;
import cn.idealer01.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import cn.idealer01.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import cn.idealer01.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import cn.idealer01.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import cn.idealer.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class OutTradeNoRuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("结算规则过滤-外部单号检验{} outTradeNo{}", requestParameter.getUserId(), requestParameter.getOutTradeNo());

        MarketPayOrderEntity marketPayOrderEntity = repository.queryMarketPayOrderEntityByOutTradeNo(requestParameter.getUserId(), requestParameter.getOutTradeNo());
        if(null == marketPayOrderEntity || TradeOrderStatusEnumVO.CLOSE.equals(marketPayOrderEntity.getTradeOrderStatusEnumVO())){
            log.error("不存在的外部交易单号或用户已退单，不需要做支付订单结算：{} outTradeNo:{}", requestParameter.getUserId(), requestParameter.getOutTradeNo());
            throw new AppException(ResponseCode.E0104);
        }

        dynamicContext.setMarketPayOrderEntity(marketPayOrderEntity);

        return next(requestParameter, dynamicContext);
    }
}
