package cn.idealer01.domain.trade.service.refund.filter;

import cn.idealer.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.idealer01.domain.trade.adapter.respository.ITradeRepository;
import cn.idealer01.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.idealer01.domain.trade.model.entity.MarketPayOrderEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundBehaviorEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundCommandEntity;
import cn.idealer01.domain.trade.service.refund.factory.TradeRefundRuleFilterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class DataNodeFilter implements ILogicHandler<TradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> {
    @Resource
    private ITradeRepository repository;

    @Override
    public TradeRefundBehaviorEntity apply(TradeRefundCommandEntity tradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("逆向流程-退单操作，数据加载节点 userId:{}, outTradeNo:{}", tradeRefundCommandEntity.getUserId(), tradeRefundCommandEntity.getOutTradeNo());
        //1.查询外部贸易单
        MarketPayOrderEntity marketPayOrderEntity = repository.queryMarketPayOrderEntityByOutTradeNo(tradeRefundCommandEntity.getUserId(), tradeRefundCommandEntity.getOutTradeNo());
        //2.查询拼团状态
        GroupBuyTeamEntity groupBuyTeamEntity = repository.queryGroupBuyTeamByTeamId(marketPayOrderEntity.getTeamId());
        //3.写入上下文（也可以使用多线程异步加载数据）
        dynamicContext.setMarketPayOrderEntity(marketPayOrderEntity);
        dynamicContext.setGroupBuyTeamEntity(groupBuyTeamEntity);

        return next(tradeRefundCommandEntity, dynamicContext);
    }
}
