package cn.idealer01.test.trigger;

import cn.idealer01.Application;
import cn.idealer01.domain.order.service.IOrderService;
import cn.idealer01.trigger.job.NoPayNotifyOrderJob;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.junit.Test;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NoPayNotifyOrderJobTest {

    @Test
    public void shouldScheduleAlipayStatusCompensationWhenPaymentNotifyIsMissed() throws Exception {
        Method exec = NoPayNotifyOrderJob.class.getDeclaredMethod("exec");

        assertNotNull(Application.class.getAnnotation(EnableScheduling.class));
        assertNotNull(exec.getAnnotation(Scheduled.class));
    }

    @Test
    public void shouldNotMarkOrderPaidWhenAlipayTradeIsStillWaitingForPayment() throws Exception {
        IOrderService orderService = mock(IOrderService.class);
        AlipayClient alipayClient = mock(AlipayClient.class);
        AlipayTradeQueryResponse response = new AlipayTradeQueryResponse();
        response.setCode("10000");
        response.setTradeStatus("WAIT_BUYER_PAY");

        when(orderService.queryNoPayNotifyOrder()).thenReturn(Collections.singletonList("order-1"));
        when(alipayClient.execute(any(AlipayTradeQueryRequest.class))).thenReturn(response);

        NoPayNotifyOrderJob job = new NoPayNotifyOrderJob();
        ReflectionTestUtils.setField(job, "orderService", orderService);
        ReflectionTestUtils.setField(job, "alipayClient", alipayClient);

        job.exec();

        verify(orderService, never()).changeOrderPaySuccess(any(), any());
    }
}
