package cn.idealer01.domain.trade.service;

import cn.idealer01.domain.trade.model.entity.MarketPayOrderEntity;
import cn.idealer01.domain.trade.model.entity.PayActivityEntity;
import cn.idealer01.domain.trade.model.entity.PayDiscountEntity;
import cn.idealer01.domain.trade.model.entity.UserEntity;
import cn.idealer01.domain.trade.model.valobj.GroupBuyProgressVO;

public interface ITradeLockOrderService {

    /**
     * 查询用户没有被支付消费完成的营销优惠订单
     * @param userId
     * @param outTradeNo
     * @return
     */
    MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo);

    /**
     * 查询拼团进度
     * @param teamId
     * @return
     */
    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    /**
     * 锁单：锁定用户商品订单
     * @param userEntity
     * @param payActivityEntity
     * @param payDiscountEntity
     * @return
     */
    MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity, PayDiscountEntity payDiscountEntity) throws Exception;
}
