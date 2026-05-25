package cn.idealer01.domain.trade.service;

import cn.idealer01.domain.trade.model.entity.NotifyTaskEntity;
import cn.idealer01.domain.trade.model.entity.TradePaySettlementEntity;
import cn.idealer01.domain.trade.model.entity.TradePaySuccessEntity;

import java.util.Map;

public interface ITradeSettlementOrderService {

    /**
     * 营销结算：对每笔支付的记账处理，直至完成拼团任务
     * @param tradePaySuccessEntity
     * @return 交易结算订单实体
     */
    TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception;

}
