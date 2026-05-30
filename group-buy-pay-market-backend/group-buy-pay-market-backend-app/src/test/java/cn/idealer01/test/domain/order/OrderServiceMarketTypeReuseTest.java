package cn.idealer01.test.domain.order;

import cn.idealer01.domain.order.adapt.port.IProductPort;
import cn.idealer01.domain.order.adapt.reposity.IOrderRepository;
import cn.idealer01.domain.order.model.aggregate.CreateOrderAggregate;
import cn.idealer01.domain.order.model.entity.OrderEntity;
import cn.idealer01.domain.order.model.entity.PayOrderEntity;
import cn.idealer01.domain.order.model.entity.ProductEntity;
import cn.idealer01.domain.order.model.entity.ShopCartEntity;
import cn.idealer01.domain.order.model.valobj.MarketPayDiscountEntity;
import cn.idealer01.domain.order.model.valobj.MarketTypeVO;
import cn.idealer01.domain.order.model.valobj.OrderStatusVO;
import cn.idealer01.domain.order.service.AbstactOrderService;
import com.alipay.api.AlipayApiException;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderServiceMarketTypeReuseTest {

    @Test
    public void createOrder_doesNotReuseExistingPayWaitOrderWhenMarketTypeDiffers() throws Exception {
        IOrderRepository repository = mock(IOrderRepository.class);
        IProductPort productPort = mock(IProductPort.class);
        when(repository.queryUnPayOrder(any())).thenReturn(OrderEntity.builder()
                .productId("9890005")
                .productName("新商品")
                .orderId("old-plain-order")
                .orderStatusVO(OrderStatusVO.PAY_WAIT)
                .payUrl("old-plain-pay-url")
                .marketType(MarketTypeVO.NO_MARKET.getCode())
                .build());
        when(productPort.queryProductByProductId("9890005")).thenReturn(ProductEntity.builder()
                .productId("9890005")
                .productName("新商品")
                .price(new BigDecimal("19.90"))
                .build());

        TestOrderService orderService = new TestOrderService(repository, productPort);
        PayOrderEntity payOrderEntity = orderService.createOrder(ShopCartEntity.builder()
                .userId("u1")
                .productId("9890005")
                .activityId(100123L)
                .marketTypeVO(MarketTypeVO.GROUP_BUY_MARKET)
                .build());

        assertEquals("new-group-pay-url", payOrderEntity.getPayUrl());
        assertEquals(Boolean.FALSE, payOrderEntity.getReusedPayOrder());
    }

    @Test
    public void createOrder_rejectsGroupBuyWhenMarketLockReturnsNoDiscount() throws Exception {
        IOrderRepository repository = mock(IOrderRepository.class);
        IProductPort productPort = mock(IProductPort.class);
        when(repository.queryUnPayOrder(any())).thenReturn(null);
        when(productPort.queryProductByProductId("9890005")).thenReturn(ProductEntity.builder()
                .productId("9890005")
                .productName("新商品")
                .price(new BigDecimal("109.00"))
                .build());

        TestOrderService orderService = new TestOrderService(repository, productPort, null);

        try {
            orderService.createOrder(ShopCartEntity.builder()
                    .userId("u1")
                    .productId("9890005")
                    .activityId(100123L)
                    .marketTypeVO(MarketTypeVO.GROUP_BUY_MARKET)
                    .build());
            fail("拼团锁单无折扣时不应降级为普通原价订单");
        } catch (IllegalStateException e) {
            assertEquals("拼团营销锁单失败", e.getMessage());
        }
        verify(repository, never()).doSaveOrder(any(CreateOrderAggregate.class));
    }

    private static class TestOrderService extends AbstactOrderService {

        private final MarketPayDiscountEntity marketPayDiscountEntity;

        TestOrderService(IOrderRepository repository, IProductPort port) {
            this(repository, port, MarketPayDiscountEntity.builder()
                    .originalPrice(new BigDecimal("19.90"))
                    .deductionPrice(new BigDecimal("10.00"))
                    .payPrice(new BigDecimal("9.90"))
                    .build());
        }

        TestOrderService(IOrderRepository repository, IProductPort port, MarketPayDiscountEntity marketPayDiscountEntity) {
            super(repository, port);
            this.marketPayDiscountEntity = marketPayDiscountEntity;
        }

        @Override
        protected PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount, MarketPayDiscountEntity marketPayDiscountEntity) throws AlipayApiException {
            return PayOrderEntity.builder()
                    .orderId(orderId)
                    .payUrl(null == marketPayDiscountEntity ? "new-plain-pay-url" : "new-group-pay-url")
                    .build();
        }

        @Override
        protected MarketPayDiscountEntity lockMarketPayOrder(String userId, String teamId, Long activityId, String productId, String orderId) {
            return marketPayDiscountEntity;
        }

        @Override
        protected PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException {
            return doPrepayOrder(userId, productId, productName, orderId, totalAmount, null);
        }

        @Override
        protected void doSaveOrder(CreateOrderAggregate orderAggregate) {
            repository.doSaveOrder(orderAggregate);
        }

        @Override
        public void changeOrderPaySuccess(String orderId, Date date) {
        }

        @Override
        public List<String> queryNoPayNotifyOrder() {
            return Collections.emptyList();
        }

        @Override
        public List<String> queryTimeoutCloseOrderList() {
            return Collections.emptyList();
        }

        @Override
        public boolean changeOrderClose(String orderId) {
            return false;
        }

        @Override
        public void changeOrderMarketSettlement(List<String> outTradeNoList) {
        }
    }
}
