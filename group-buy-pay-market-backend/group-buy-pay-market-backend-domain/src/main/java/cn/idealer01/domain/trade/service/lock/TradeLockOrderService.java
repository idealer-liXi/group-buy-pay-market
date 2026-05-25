package cn.idealer01.domain.trade.service.lock;

import cn.idealer01.domain.trade.adapter.respository.ITradeRepository;
import cn.idealer01.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import cn.idealer01.domain.trade.model.entity.*;
import cn.idealer01.domain.trade.model.valobj.GroupBuyProgressVO;
import cn.idealer01.domain.trade.service.ITradeLockOrderService;
import cn.idealer01.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import cn.idealer.wrench.design.framework.link.model2.chain.BusinessLinkedList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class TradeLockOrderService implements ITradeLockOrderService {
    @Resource
    private ITradeRepository tradeRepository;
    @Resource
    private BusinessLinkedList<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> tradeRuleFilter;
    @Override
    public MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo) {
        log.info("拼团交易-查询未支付的订单：{} outTradeNo:{}", userId, outTradeNo);
        return tradeRepository.queryMarketPayOrderEntityByOutTradeNo(userId, outTradeNo);
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        log.info("拼团交易-查询拼单进度：{}", teamId);
        return tradeRepository.queryGroupBuyProgress(teamId);
    }

    @Override
    public MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity, PayDiscountEntity payDiscountEntity) throws Exception {
        log.info("拼团交易-锁定营销优惠支付订单:{} activityId:{} goodsId:{}", userEntity.getUserId(),payActivityEntity.getActivityId(), payDiscountEntity.getGoodsId() );

        //责任链过滤
        TradeLockRuleFilterBackEntity tradeRuleFilterBackEntity = tradeRuleFilter.apply(TradeLockRuleCommandEntity.builder()
                        .activityId(payActivityEntity.getActivityId())
                        .userId(userEntity.getUserId())
                        .teamId(payActivityEntity.getTeamId())
                        .build(),
                new TradeLockRuleFilterFactory.DynamicContext());

        Integer userTakeOrderCount = tradeRuleFilterBackEntity.getUserTakeOrderCount();

        try {
            return tradeRepository.lockMarketPayOrder(GroupBuyOrderAggregate.builder()
                    .userEntity(userEntity)
                    .payActivityEntity(payActivityEntity)
                    .payDiscountEntity(payDiscountEntity)
                    .userTakeOrderCount(userTakeOrderCount)
                    .build());
        } catch (Exception e){
            //数据库锁单失败，在redis中恢复名额
            tradeRepository.recoveryTeamStock(tradeRuleFilterBackEntity.getRecoveryTeamStockKey(), payActivityEntity.getValidTime());
            throw e;
        }
    }
}
