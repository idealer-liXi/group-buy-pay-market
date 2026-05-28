package cn.idealer01.test.trigger;

import cn.idealer01.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundBehaviorEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundCommandEntity;
import cn.idealer01.domain.trade.model.valobj.TeamRefundSuccess;
import cn.idealer01.domain.trade.service.ITradeRefundOrderService;
import cn.idealer01.trigger.job.TimeoutRefundJob;
import org.junit.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimeoutRefundJobTest {

    @Test
    public void exec_refundsPaidExpiredUnformedOrders() throws Exception {
        TimeoutRefundJob job = new TimeoutRefundJob();
        FakeRefundService refundService = new FakeRefundService();
        RedissonClient redissonClient = mock(RedissonClient.class);
        RLock lock = mock(RLock.class);
        when(redissonClient.getLock("group_buy_market_timeout_refund_job_exec")).thenReturn(lock);
        when(lock.tryLock(3, 60, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isLocked()).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        setField(job, "tradeRefundOrderService", refundService);
        setField(job, "redissonClient", redissonClient);

        job.exec();

        assertEquals(1, refundService.refundedOutTradeNoList.size());
        assertEquals("paid-expired-1", refundService.refundedOutTradeNoList.get(0));
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static class FakeRefundService implements ITradeRefundOrderService {
        private final List<String> refundedOutTradeNoList = new ArrayList<>();

        @Override
        public TradeRefundBehaviorEntity refundOrder(TradeRefundCommandEntity command) {
            refundedOutTradeNoList.add(command.getOutTradeNo());
            return TradeRefundBehaviorEntity.builder()
                    .userId(command.getUserId())
                    .tradeRefundBehaviorEnum(TradeRefundBehaviorEntity.TradeRefundBehaviorEnum.SUCCESS)
                    .build();
        }

        @Override
        public void restoreTeamLockStock(TeamRefundSuccess teamRefundSuccess) {
        }

        @Override
        public List<UserGroupBuyOrderDetailEntity> queryTimeoutUnpaidOrderList() {
            return Collections.emptyList();
        }

        @Override
        public List<UserGroupBuyOrderDetailEntity> queryTimeoutPaidUnformedOrderList() {
            return Collections.singletonList(UserGroupBuyOrderDetailEntity.builder()
                    .userId("u1")
                    .source("s01")
                    .channel("c01")
                    .outTradeNo("paid-expired-1")
                    .build());
        }
    }
}
