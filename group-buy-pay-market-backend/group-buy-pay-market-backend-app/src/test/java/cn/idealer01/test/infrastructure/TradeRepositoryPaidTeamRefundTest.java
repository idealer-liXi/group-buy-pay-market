package cn.idealer01.test.infrastructure;

import cn.idealer01.domain.trade.model.aggregate.GroupBuyRefundAggregate;
import cn.idealer01.domain.trade.model.entity.NotifyTaskEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundOrderEntity;
import cn.idealer01.infrastructure.adapter.repository.TradeRepository;
import cn.idealer01.infrastructure.dao.IGroupBuyActivityDao;
import cn.idealer01.infrastructure.dao.IGroupBuyOrderDao;
import cn.idealer01.infrastructure.dao.IGroupBuyOrderListDao;
import cn.idealer01.infrastructure.dao.INotifyTaskDao;
import cn.idealer01.infrastructure.dao.IOrderDao;
import cn.idealer01.infrastructure.dao.po.NotifyTask;
import cn.idealer01.types.enums.GroupBuyOrderEnumVO;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TradeRepositoryPaidTeamRefundTest {

    @Test
    public void paidTeam2Refund_closesRefundedPayOrderAndKeepsCompletedTeamStatus() {
        TradeRepository repository = new TradeRepository();
        IGroupBuyOrderListDao groupBuyOrderListDao = mock(IGroupBuyOrderListDao.class);
        IGroupBuyOrderDao groupBuyOrderDao = mock(IGroupBuyOrderDao.class);
        INotifyTaskDao notifyTaskDao = mock(INotifyTaskDao.class);
        IOrderDao orderDao = mock(IOrderDao.class);
        ReflectionTestUtils.setField(repository, "groupBuyOrderListDao", groupBuyOrderListDao);
        ReflectionTestUtils.setField(repository, "groupBuyOrderDao", groupBuyOrderDao);
        ReflectionTestUtils.setField(repository, "groupBuyActivityDao", mock(IGroupBuyActivityDao.class));
        ReflectionTestUtils.setField(repository, "notifyTaskDao", notifyTaskDao);
        ReflectionTestUtils.setField(repository, "orderDao", orderDao);
        ReflectionTestUtils.setField(repository, "topic_team_refund", "topic.team_refund");

        when(groupBuyOrderListDao.paidTeam2Refund(any())).thenReturn(1);
        when(groupBuyOrderDao.paidTeam2Refund(any())).thenReturn(1);
        when(groupBuyOrderDao.paidTeam2RefundFail(any())).thenReturn(1);
        when(orderDao.changeOrderClose("466131550742")).thenReturn(true);

        NotifyTaskEntity notifyTask = repository.paidTeam2Refund(GroupBuyRefundAggregate.buildPaidTeam2RefundAggregate(
                TradeRefundOrderEntity.builder()
                        .userId("u1")
                        .orderId("082426452336")
                        .teamId("52179130")
                        .activityId(9890003L)
                        .outTradeNo("466131550742")
                        .build(),
                -1,
                -1,
                GroupBuyOrderEnumVO.COMPLETE_FAIL));

        assertNotNull(notifyTask);
        verify(orderDao).changeOrderClose("466131550742");
        verify(groupBuyOrderDao, never()).paidTeam2Refund(any());
        verify(groupBuyOrderDao, never()).paidTeam2RefundFail(any());

        ArgumentCaptor<NotifyTask> notifyTaskCaptor = ArgumentCaptor.forClass(NotifyTask.class);
        verify(notifyTaskDao).insert(notifyTaskCaptor.capture());
        String parameterJson = notifyTaskCaptor.getValue().getParameterJson();
        assertTrue(parameterJson.contains("\"type\":\"paid_formed\""));
        assertFalse(parameterJson.contains("PAID_FORMED"));
    }
}
