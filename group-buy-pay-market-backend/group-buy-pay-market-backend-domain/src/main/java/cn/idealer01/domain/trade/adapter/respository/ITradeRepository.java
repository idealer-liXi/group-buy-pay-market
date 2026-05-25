package cn.idealer01.domain.trade.adapter.respository;

import cn.idealer01.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.idealer01.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import cn.idealer01.domain.trade.model.aggregate.GroupBuyRefundAggregate;
import cn.idealer01.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import cn.idealer01.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.idealer01.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.idealer01.domain.trade.model.entity.MarketPayOrderEntity;
import cn.idealer01.domain.trade.model.entity.NotifyTaskEntity;
import cn.idealer01.domain.trade.model.valobj.GroupBuyProgressVO;

import java.util.List;

public interface ITradeRepository {
    MarketPayOrderEntity queryMarketPayOrderEntityByOutTradeNo(String userId, String outTradeNo);

    MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate);

    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    GroupBuyActivityEntity queryGroupBuyActivityEntityByActivityId(Long activityId);

    Integer queryOrderCountByActivityId(Long activityId, String userId);

    GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId);

    NotifyTaskEntity settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate);

    boolean isSCBlackIntercept(String source, String channel);

    /**
     * 查询所有未执行通知回调的任务列表
     * @return 任务列表
     */
    List<NotifyTaskEntity> queryUnExecutedNotifyTaskList();

    List<NotifyTaskEntity> queryUnExecutedNotifyTaskList(String teamId);

    int updateNotifyTaskStatusSuccess(NotifyTaskEntity notifyTaskEntity);

    int updateNotifyTaskStatusError(NotifyTaskEntity notifyTaskEntity);

    int updateNotifyTaskStatusRetry(NotifyTaskEntity notifyTaskEntity);

    boolean occupyTeamStock(String teamStockKey, String recoveryTeamStockKey, Integer target, Integer validTime);

    void recoveryTeamStock(String recoveryTeamStockKey, Integer validTime);

    NotifyTaskEntity unpaid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate);

    NotifyTaskEntity paid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate);

    NotifyTaskEntity paidTeam2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate);

    void refund2AddRecovery(String recoveryTeamStockKey, String orderId);

    List<UserGroupBuyOrderDetailEntity> queryTimeoutUnpaidOrderList();

}
