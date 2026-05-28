package cn.idealer01.domain.trade.service.refund;

import cn.idealer.wrench.design.framework.link.model2.chain.BusinessLinkedList;
import cn.idealer.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.idealer01.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.idealer01.domain.trade.adapter.respository.ITradeRepository;
import cn.idealer01.domain.trade.model.entity.*;
import cn.idealer01.domain.trade.model.valobj.RefundTypeEnumVO;
import cn.idealer01.domain.trade.model.valobj.TeamRefundSuccess;
import cn.idealer01.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import cn.idealer01.domain.trade.service.ITradeRefundOrderService;
import cn.idealer01.domain.trade.service.refund.business.IRefundOrderStrategy;
import cn.idealer01.domain.trade.service.refund.factory.TradeRefundRuleFilterFactory;
import cn.idealer01.types.enums.GroupBuyOrderEnumVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TradeRefundOrderService implements ITradeRefundOrderService {
    private final Map<String, IRefundOrderStrategy>  refundOrderStrategyMap;

    private final ITradeRepository repository;

    public TradeRefundOrderService(ITradeRepository repository, Map<String, IRefundOrderStrategy> refundOrderStrategyMap) {
        this.repository = repository;
        this.refundOrderStrategyMap = refundOrderStrategyMap;
    }

    @Resource
    private BusinessLinkedList<TradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> tradeRefundRuleFilter;

    @Override
    public TradeRefundBehaviorEntity refundOrder(TradeRefundCommandEntity tradeRefundCommandEntity) throws Exception {
        log.info("逆向流程，退单 userId:{}, outTradeNo:{}", tradeRefundCommandEntity.getUserId(), tradeRefundCommandEntity.getOutTradeNo());
        return tradeRefundRuleFilter.apply(tradeRefundCommandEntity, new TradeRefundRuleFilterFactory.DynamicContext());
    }

    @Override
    public void restoreTeamLockStock(TeamRefundSuccess teamRefundSuccess) throws Exception {
        log.info("逆向流程，恢复锁单量 userId:{} teamId:{} activityId:{}", teamRefundSuccess.getUserId(), teamRefundSuccess.getTeamId(), teamRefundSuccess.getActivityId());

        //根据枚举值获取对应的策略
        RefundTypeEnumVO refundTypeEnumVO = RefundTypeEnumVO.getRefundTypeEnumVOByCode(teamRefundSuccess.getType());
        IRefundOrderStrategy refundOrderStrategy = refundOrderStrategyMap.get(refundTypeEnumVO.getStrategy());

        //逆向库存操作，恢复退单量
        refundOrderStrategy.reverseStock(teamRefundSuccess);

    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> queryTimeoutUnpaidOrderList() {
        log.info("扫描数据，超时组队未支付订单");
        return repository.queryTimeoutUnpaidOrderList();
    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> queryTimeoutPaidUnformedOrderList() {
        log.info("扫描数据，超时已支付未成团订单");
        return repository.queryTimeoutPaidUnformedOrderList();
    }
}
