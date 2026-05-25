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
import cn.idealer01.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service("paid2RefundStrategy")
public class Paid2RefundStrategy extends AbstractRefundOrderStrategy {

    @Override
    public void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity) {
        log.info("退单，已支付 未成团，userId:{} teamId:{} orderId:{}", tradeRefundOrderEntity.getUserId(), tradeRefundOrderEntity.getTeamId(), tradeRefundOrderEntity.getOrderId());

        //1.执行退单
        NotifyTaskEntity notifyTask = repository.paid2Refund(GroupBuyRefundAggregate.buildPaid2RefundAggregate(tradeRefundOrderEntity, -1, -1));

        //2.回调通知
        sendRefundNotifyMessage(notifyTask, RefundTypeEnumVO.PAID_UNFORMED.getInfo());

    }

    @Override
    public void reverseStock(TeamRefundSuccess teamRefundSuccess) {
        doReverseStock(teamRefundSuccess, RefundTypeEnumVO.PAID_UNFORMED.getStrategy());
    }
}
