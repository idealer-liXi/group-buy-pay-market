package cn.idealer01.domain.trade.service.refund.business.impl;

import cn.idealer01.domain.trade.adapter.respository.ITradeRepository;
import cn.idealer01.domain.trade.model.aggregate.GroupBuyRefundAggregate;
import cn.idealer01.domain.trade.model.entity.NotifyTaskEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundOrderEntity;
import cn.idealer01.domain.trade.model.valobj.RefundTypeEnumVO;
import cn.idealer01.domain.trade.model.valobj.TeamRefundSuccess;
import cn.idealer01.domain.trade.service.ITradeTaskService;
import cn.idealer01.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import cn.idealer01.domain.trade.service.refund.business.AbstractRefundOrderStrategy;
import cn.idealer01.domain.trade.service.refund.business.IRefundOrderStrategy;
import cn.idealer01.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service("unpaid2RefundStrategy")
public class Unpaid2RefundStrategy extends AbstractRefundOrderStrategy {

    @Override
    public void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity) {
        log.info("退单：未支付，未成团 userId:{} orderId:{} teamId:{}", tradeRefundOrderEntity.getUserId(), tradeRefundOrderEntity.getOrderId(), tradeRefundOrderEntity.getTeamId());
        NotifyTaskEntity notifyTaskEntity = repository.unpaid2Refund(GroupBuyRefundAggregate.buildUnpaid2RefundAggregate(tradeRefundOrderEntity, -1));
        // 2. 发送MQ消息 - 发送MQ，恢复锁单库存量使用
        sendRefundNotifyMessage(notifyTaskEntity, RefundTypeEnumVO.UNPAID_UNLOCK.getInfo());
    }

    @Override
    public void reverseStock(TeamRefundSuccess teamRefundSuccess) {
        doReverseStock(teamRefundSuccess, RefundTypeEnumVO.UNPAID_UNLOCK.getInfo());
    }
}
