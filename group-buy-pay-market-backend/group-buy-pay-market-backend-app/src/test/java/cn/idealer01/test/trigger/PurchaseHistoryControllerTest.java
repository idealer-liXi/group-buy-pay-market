package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.CancelOrderRequestDTO;
import cn.idealer01.api.dto.PurchaseHistoryResponseDTO;
import cn.idealer01.api.dto.RefundPaidOrderRequestDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.IOrderDao;
import cn.idealer01.infrastructure.dao.po.PayOrder;
import cn.idealer01.trigger.http.PurchaseHistoryController;
import cn.idealer01.trigger.websocket.UserNotificationWebSocketHandler;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PurchaseHistoryControllerTest {

    @Test
    public void queryPurchaseHistory_returnsRecordsGroupedByPurchaseAndStatusType() {
        IOrderDao orderDao = mock(IOrderDao.class);
        when(orderDao.queryPayOrderListByUserId("u1")).thenReturn(Arrays.asList(
                PayOrder.builder().orderId("o0").productId("9890000").productName("刚创建订单").status("CREATE").marketType(1).totalAmount(new BigDecimal("9.90")).payAmount(new BigDecimal("9.90")).build(),
                PayOrder.builder().orderId("o1").productId("9890001").productName("拼团待支付商品").status("PAY_WAIT").marketType(1).payUrl("pay-form-1").totalAmount(new BigDecimal("99.00")).payAmount(new BigDecimal("79.00")).build(),
                PayOrder.builder().orderId("o2").productId("9890002").productName("拼团待成团商品").status("PAY_SUCCESS").marketType(1).groupOrderStatus(0).groupValidEndTime(new Date(System.currentTimeMillis() + 60000)).totalAmount(new BigDecimal("89.90")).payAmount(new BigDecimal("79.90")).build(),
                PayOrder.builder().orderId("o3").productId("9890005").productName("普通商品").status("PAY_SUCCESS").marketType(0).totalAmount(new BigDecimal("19.90")).payAmount(new BigDecimal("19.90")).build(),
                PayOrder.builder().orderId("o4").productId("9890006").productName("拼团成功商品").status("MARKET").marketType(1).totalAmount(new BigDecimal("29.90")).payAmount(new BigDecimal("19.90")).build(),
                PayOrder.builder().orderId("o5").productId("9890007").productName("关闭商品").status("CLOSE").marketType(0).totalAmount(new BigDecimal("29.90")).payAmount(new BigDecimal("29.90")).build(),
                PayOrder.builder().orderId("o6").productId("9890008").productName("已退拼团商品").status("PAY_SUCCESS").marketType(1).groupOrderListStatus(2).totalAmount(new BigDecimal("39.90")).payAmount(new BigDecimal("29.90")).build(),
                PayOrder.builder().orderId("o7").productId("9890009").productName("超时拼团商品").status("PAY_SUCCESS").marketType(1).groupOrderStatus(0).groupValidEndTime(new Date(System.currentTimeMillis() - 60000)).totalAmount(new BigDecimal("49.90")).payAmount(new BigDecimal("39.90")).build(),
                PayOrder.builder().orderId("o8").productId("9890010").productName("队伍完成商品").status("PAY_SUCCESS").marketType(1).groupOrderStatus(1).totalAmount(new BigDecimal("59.90")).payAmount(new BigDecimal("49.90")).build()
        ));

        PurchaseHistoryController controller = new PurchaseHistoryController(orderDao);
        Response<PurchaseHistoryResponseDTO> response = controller.queryPurchaseHistory("u1");

        assertEquals("0000", response.getCode());
        assertEquals(9, response.getData().getRecordList().size());
        assertEquals("GROUP_BUY", response.getData().getRecordList().get(0).getPurchaseType());
        assertEquals("WAIT_PAY", response.getData().getRecordList().get(0).getStatusType());
        assertEquals("o0", response.getData().getRecordList().get(0).getOutTradeNo());
        assertEquals("pay-form-1", response.getData().getRecordList().get(1).getPayUrl());
        assertEquals("o1", response.getData().getRecordList().get(1).getOutTradeNo());
        assertEquals("GROUP_WAIT", response.getData().getRecordList().get(2).getStatusType());
        assertEquals("GROUP_SUCCESS", response.getData().getRecordList().get(3).getStatusType());
        assertEquals("GROUP_SUCCESS", response.getData().getRecordList().get(4).getStatusType());
        assertEquals("CLOSED", response.getData().getRecordList().get(5).getStatusType());
        assertEquals("CLOSED", response.getData().getRecordList().get(6).getStatusType());
        assertEquals("CLOSED", response.getData().getRecordList().get(7).getStatusType());
        assertEquals("GROUP_SUCCESS", response.getData().getRecordList().get(8).getStatusType());
    }

    @Test
    public void cancelOrder_closesOwnUnpaidOrder() {
        IOrderDao orderDao = mock(IOrderDao.class);
        when(orderDao.queryOrderByOrderId("o1")).thenReturn(PayOrder.builder()
                .userId("u1")
                .orderId("o1")
                .marketType(0)
                .status("PAY_WAIT")
                .build());
        when(orderDao.changeOrderClose("o1")).thenReturn(true);

        PurchaseHistoryController controller = new PurchaseHistoryController(orderDao);
        Response<Boolean> response = controller.cancelOrder(CancelOrderRequestDTO.builder()
                .userId("u1")
                .orderId("o1")
                .build());

        assertEquals("0000", response.getCode());
        assertEquals(Boolean.TRUE, response.getData());
        verify(orderDao).changeOrderClose("o1");
    }

    @Test
    public void cancelOrder_rejectsOtherUsersOrder() {
        IOrderDao orderDao = mock(IOrderDao.class);
        when(orderDao.queryOrderByOrderId("o1")).thenReturn(PayOrder.builder()
                .userId("u2")
                .orderId("o1")
                .marketType(0)
                .status("PAY_WAIT")
                .build());

        PurchaseHistoryController controller = new PurchaseHistoryController(orderDao);
        Response<Boolean> response = controller.cancelOrder(CancelOrderRequestDTO.builder()
                .userId("u1")
                .orderId("o1")
                .build());

        assertEquals("0002", response.getCode());
        verify(orderDao, never()).changeOrderClose("o1");
    }

    @Test
    public void cancelOrder_rejectsGroupBuyOrder() {
        IOrderDao orderDao = mock(IOrderDao.class);
        when(orderDao.queryOrderByOrderId("o1")).thenReturn(PayOrder.builder()
                .userId("u1")
                .orderId("o1")
                .marketType(1)
                .status("PAY_WAIT")
                .build());

        PurchaseHistoryController controller = new PurchaseHistoryController(orderDao);
        Response<Boolean> response = controller.cancelOrder(CancelOrderRequestDTO.builder()
                .userId("u1")
                .orderId("o1")
                .build());

        assertEquals("0002", response.getCode());
        verify(orderDao, never()).changeOrderClose("o1");
    }

    @Test
    public void refundPaidOrder_closesOwnPaidPlainOrderWithoutAlipayByDefault() throws Exception {
        IOrderDao orderDao = mock(IOrderDao.class);
        AlipayClient alipayClient = mock(AlipayClient.class);
        UserNotificationWebSocketHandler webSocketHandler = mock(UserNotificationWebSocketHandler.class);
        when(orderDao.queryPayOrderByOrderId("o1")).thenReturn(PayOrder.builder()
                .userId("u1")
                .orderId("o1")
                .marketType(0)
                .status("PAY_SUCCESS")
                .payAmount(new BigDecimal("19.90"))
                .build());
        when(orderDao.changeOrderClose("o1")).thenReturn(true);

        PurchaseHistoryController controller = new PurchaseHistoryController(orderDao, alipayClient, webSocketHandler);
        Response<Boolean> response = controller.refundPaidOrder(RefundPaidOrderRequestDTO.builder()
                .userId("u1")
                .orderId("o1")
                .build());

        assertEquals("0000", response.getCode());
        assertEquals(Boolean.TRUE, response.getData());
        verify(alipayClient, never()).execute(any(AlipayTradeRefundRequest.class));
        verify(orderDao).changeOrderClose("o1");
        verify(webSocketHandler).sendToUsers(eq(Collections.singleton("u1")), contains("REFUND_SUCCESS"));
    }

    @Test
    public void refundPaidOrder_refundsAlipayThenClosesOwnPaidPlainOrderWhenEnabled() throws Exception {
        IOrderDao orderDao = mock(IOrderDao.class);
        AlipayClient alipayClient = mock(AlipayClient.class);
        when(orderDao.queryPayOrderByOrderId("o1")).thenReturn(PayOrder.builder()
                .userId("u1")
                .orderId("o1")
                .marketType(0)
                .status("PAY_SUCCESS")
                .payAmount(new BigDecimal("19.90"))
                .build());
        AlipayTradeRefundResponse refundResponse = new AlipayTradeRefundResponse();
        refundResponse.setCode("10000");
        when(alipayClient.execute(any(AlipayTradeRefundRequest.class))).thenReturn(refundResponse);
        when(orderDao.changeOrderClose("o1")).thenReturn(true);

        PurchaseHistoryController controller = new PurchaseHistoryController(orderDao, alipayClient);
        ReflectionTestUtils.setField(controller, "alipayRefundEnabled", true);
        Response<Boolean> response = controller.refundPaidOrder(RefundPaidOrderRequestDTO.builder()
                .userId("u1")
                .orderId("o1")
                .build());

        assertEquals("0000", response.getCode());
        assertEquals(Boolean.TRUE, response.getData());
        verify(alipayClient).execute(any(AlipayTradeRefundRequest.class));
        verify(orderDao).changeOrderClose("o1");
    }

    @Test
    public void refundPaidOrder_doesNotCloseWhenAlipayRefundFails() throws Exception {
        IOrderDao orderDao = mock(IOrderDao.class);
        AlipayClient alipayClient = mock(AlipayClient.class);
        when(orderDao.queryPayOrderByOrderId("o1")).thenReturn(PayOrder.builder()
                .userId("u1")
                .orderId("o1")
                .marketType(0)
                .status("PAY_SUCCESS")
                .payAmount(new BigDecimal("19.90"))
                .build());
        AlipayTradeRefundResponse refundResponse = new AlipayTradeRefundResponse();
        refundResponse.setCode("40004");
        refundResponse.setSubMsg("退款失败");
        when(alipayClient.execute(any(AlipayTradeRefundRequest.class))).thenReturn(refundResponse);

        PurchaseHistoryController controller = new PurchaseHistoryController(orderDao, alipayClient);
        ReflectionTestUtils.setField(controller, "alipayRefundEnabled", true);
        Response<Boolean> response = controller.refundPaidOrder(RefundPaidOrderRequestDTO.builder()
                .userId("u1")
                .orderId("o1")
                .build());

        assertEquals("0001", response.getCode());
        verify(orderDao, never()).changeOrderClose("o1");
    }

    @Test
    public void refundPaidOrder_rejectsOtherUsersOrder() throws Exception {
        IOrderDao orderDao = mock(IOrderDao.class);
        AlipayClient alipayClient = mock(AlipayClient.class);
        when(orderDao.queryPayOrderByOrderId("o1")).thenReturn(PayOrder.builder()
                .userId("u2")
                .orderId("o1")
                .marketType(0)
                .status("PAY_SUCCESS")
                .payAmount(new BigDecimal("19.90"))
                .build());

        PurchaseHistoryController controller = new PurchaseHistoryController(orderDao, alipayClient);
        Response<Boolean> response = controller.refundPaidOrder(RefundPaidOrderRequestDTO.builder()
                .userId("u1")
                .orderId("o1")
                .build());

        assertEquals("0002", response.getCode());
        verify(alipayClient, never()).execute(any(AlipayTradeRefundRequest.class));
        verify(orderDao, never()).changeOrderClose("o1");
    }

    @Test
    public void refundPaidOrder_rejectsGroupBuyOrder() throws Exception {
        IOrderDao orderDao = mock(IOrderDao.class);
        AlipayClient alipayClient = mock(AlipayClient.class);
        when(orderDao.queryPayOrderByOrderId("o1")).thenReturn(PayOrder.builder()
                .userId("u1")
                .orderId("o1")
                .marketType(1)
                .status("PAY_SUCCESS")
                .payAmount(new BigDecimal("19.90"))
                .build());

        PurchaseHistoryController controller = new PurchaseHistoryController(orderDao, alipayClient);
        Response<Boolean> response = controller.refundPaidOrder(RefundPaidOrderRequestDTO.builder()
                .userId("u1")
                .orderId("o1")
                .build());

        assertEquals("0002", response.getCode());
        verify(alipayClient, never()).execute(any(AlipayTradeRefundRequest.class));
        verify(orderDao, never()).changeOrderClose("o1");
    }

    @Test
    public void refundPaidOrder_rejectsUnpaidPlainOrder() throws Exception {
        IOrderDao orderDao = mock(IOrderDao.class);
        AlipayClient alipayClient = mock(AlipayClient.class);
        when(orderDao.queryPayOrderByOrderId("o1")).thenReturn(PayOrder.builder()
                .userId("u1")
                .orderId("o1")
                .marketType(0)
                .status("PAY_WAIT")
                .payAmount(new BigDecimal("19.90"))
                .build());

        PurchaseHistoryController controller = new PurchaseHistoryController(orderDao, alipayClient);
        Response<Boolean> response = controller.refundPaidOrder(RefundPaidOrderRequestDTO.builder()
                .userId("u1")
                .orderId("o1")
                .build());

        assertEquals("0002", response.getCode());
        verify(alipayClient, never()).execute(any(AlipayTradeRefundRequest.class));
        verify(orderDao, never()).changeOrderClose("o1");
    }
}
