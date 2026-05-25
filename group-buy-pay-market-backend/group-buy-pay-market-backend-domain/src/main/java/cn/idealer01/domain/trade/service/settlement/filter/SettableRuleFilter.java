package cn.idealer01.domain.trade.service.settlement.filter;

import cn.idealer01.domain.trade.adapter.respository.ITradeRepository;
import cn.idealer01.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.idealer01.domain.trade.model.entity.MarketPayOrderEntity;
import cn.idealer01.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import cn.idealer01.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import cn.idealer01.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import cn.idealer.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class SettableRuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {
    @Resource
    private ITradeRepository repository;
    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("结算规则过滤-有效时间检验{} outTradeNo:{}", requestParameter.getUserId(), requestParameter.getOutTradeNo());

        //获取上下文数据
        MarketPayOrderEntity marketPayOrderEntity = dynamicContext.getMarketPayOrderEntity();

        //查询拼团对象
        GroupBuyTeamEntity groupBuyTeamEntity = repository.queryGroupBuyTeamByTeamId(marketPayOrderEntity.getTeamId());

        //外部交易时间 - 用户支付完成时间要在拼团有效时间范围内
        Date outTradeTime = requestParameter.getOutTradeTime();

        if(!outTradeTime.before(groupBuyTeamEntity.getValidEndTime())){
            log.error("订单交易不在拼团有效时间范围内");
            throw new AppException(ResponseCode.E0106);
        }

        //设置上下文
        dynamicContext.setGroupBuyTeamEntity(groupBuyTeamEntity);

        return next(requestParameter, dynamicContext);
    }
}
