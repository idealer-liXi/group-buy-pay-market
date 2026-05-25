package cn.idealer01.domain.trade.service;

import cn.idealer01.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundBehaviorEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundCommandEntity;
import cn.idealer01.domain.trade.model.valobj.TeamRefundSuccess;

import java.util.List;

public interface ITradeRefundOrderService {

    TradeRefundBehaviorEntity refundOrder(TradeRefundCommandEntity tradeRefundCommandEntity) throws Exception;

    void restoreTeamLockStock(TeamRefundSuccess teamRefundSuccess) throws Exception;

    List<UserGroupBuyOrderDetailEntity> queryTimeoutUnpaidOrderList();
}
