package cn.idealer01.domain.trade.service.settlement.filter;

import cn.idealer01.domain.trade.adapter.respository.ITradeRepository;
import cn.idealer01.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import cn.idealer01.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import cn.idealer01.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import cn.idealer.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class SCRuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {
    @Resource
    private ITradeRepository repository;

    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("结算规则规律-SC黑名单校验{} outTradeNo{}", requestParameter.getUserId(), requestParameter.getOutTradeNo());

        boolean intercept = repository.isSCBlackIntercept(requestParameter.getSource(), requestParameter.getChannel());
        if(intercept){
            log.error("{} {}渠道黑名单拦截", requestParameter.getSource(), requestParameter.getChannel());
            throw new AppException(ResponseCode.E0105);
        }


        return next(requestParameter, dynamicContext);
    }
}
