package cn.idealer01.test.domain.trade;

import cn.idealer01.domain.trade.adapter.respository.ITradeRepository;
import cn.idealer01.domain.trade.model.valobj.TeamRefundSuccess;
import cn.idealer01.domain.trade.service.refund.TradeRefundOrderService;
import cn.idealer01.domain.trade.service.refund.business.IRefundOrderStrategy;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TradeRefundOrderServiceTest {

    @Test
    public void restoreTeamLockStock_acceptsEnumNamePaidFormedRefund() throws Exception {
        IRefundOrderStrategy paidTeam2RefundStrategy = mock(IRefundOrderStrategy.class);
        Map<String, IRefundOrderStrategy> refundOrderStrategyMap = new HashMap<>();
        refundOrderStrategyMap.put("paidTeam2RefundStrategy", paidTeam2RefundStrategy);
        TradeRefundOrderService service = new TradeRefundOrderService(mock(ITradeRepository.class), refundOrderStrategyMap);
        TeamRefundSuccess teamRefundSuccess = TeamRefundSuccess.builder()
                .type("PAID_FORMED")
                .userId("u1")
                .teamId("t1")
                .activityId(9890003L)
                .orderId("o1")
                .outTradeNo("trade1")
                .build();

        service.restoreTeamLockStock(teamRefundSuccess);

        verify(paidTeam2RefundStrategy).reverseStock(teamRefundSuccess);
    }
}
