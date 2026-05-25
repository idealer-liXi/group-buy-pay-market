package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.order.adapt.event.PaySuccessMessageEvent;
import cn.idealer01.domain.order.adapt.reposity.IOrderRepository;
import cn.idealer01.domain.order.model.aggregate.CreateOrderAggregate;
import cn.idealer01.domain.order.model.entity.OrderEntity;
import cn.idealer01.domain.order.model.entity.PayOrderEntity;
import cn.idealer01.domain.order.model.entity.ProductEntity;
import cn.idealer01.domain.order.model.entity.ShopCartEntity;
import cn.idealer01.domain.order.model.valobj.MarketTypeVO;
import cn.idealer01.domain.order.model.valobj.OrderStatusVO;
import cn.idealer01.infrastructure.dao.IOrderDao;
import cn.idealer01.infrastructure.dao.po.PayOrder;
import cn.idealer01.infrastructure.event.EventPublisher;
import cn.idealer01.types.event.BaseEvent;
import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.EventBus;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Repository
public class OrderReposity implements IOrderRepository {

    @Resource
    private IOrderDao orderDao;

    @Resource
    private PaySuccessMessageEvent paySuccessMessageEvent;

//    @Resource
//    private EventBus eventBus;
    @Resource
    private EventPublisher eventPublisher;

    @Override
    public OrderEntity queryUnPayOrder(ShopCartEntity shopCartEntity) {
        // 1. 封装参数
        PayOrder orderReq = new PayOrder();
        orderReq.setUserId(shopCartEntity.getUserId());
        orderReq.setProductId(shopCartEntity.getProductId());

        // 2. 查询到订单
        PayOrder order = orderDao.queryUnPayOrder(orderReq);
        if (null == order) return null;

        // 3. 返回结果
        return OrderEntity.builder()
                .productId(order.getProductId())
                .productName(order.getProductName())
                .orderId(order.getOrderId())
                .orderStatusVO(OrderStatusVO.valueOf(order.getStatus()))
                .orderTime(order.getOrderTime())
                .totalAmount(order.getTotalAmount())
                .payUrl(order.getPayUrl())
                .marketType(order.getMarketType())
                .marketDeductionAmount(order.getMarketDeductionAmount())
                .payAmount(order.getPayAmount())
                .build();

    }

    @Override
    public void doSaveOrder(CreateOrderAggregate orderAggregate) {
        String userId = orderAggregate.getUserId();
        ProductEntity productEntity = orderAggregate.getProductEntity();
        OrderEntity orderEntity = orderAggregate.getOrderEntity();

        PayOrder order = new PayOrder();
        order.setUserId(userId);
        order.setProductId(productEntity.getProductId());
        order.setProductName(productEntity.getProductName());
        order.setOrderId(orderEntity.getOrderId());
        order.setOrderTime(orderEntity.getOrderTime());
        order.setTotalAmount(productEntity.getPrice());
        order.setStatus(orderEntity.getOrderStatusVO().getCode());
        order.setMarketType(orderEntity.getMarketType());
        order.setMarketDeductionAmount(BigDecimal.ZERO);
        order.setPayAmount(productEntity.getPrice());

        orderDao.insert(order);
    }

    @Override
    public void updateOrderPayInfo(PayOrderEntity payOrderEntity) {
        PayOrder payOrderReq = PayOrder.builder()
                .userId(payOrderEntity.getUserId())
                .orderId(payOrderEntity.getOrderId())
                .status(payOrderEntity.getOrderStatus().getCode())
                .payUrl(payOrderEntity.getPayUrl())
                .marketType(payOrderEntity.getMarketType())
                .marketDeductionAmount(payOrderEntity.getMarketDeductionAmount())
                .payAmount(payOrderEntity.getPayAmount())
                .build();

        orderDao.updateOrderPayInfo(payOrderReq);
    }

    @Override
    public void changeOrderPaySuccess(String orderId, Date payTime) {
        //1.封装参数
        PayOrder payOrderReq = new PayOrder();
        payOrderReq.setOrderId(orderId);
        payOrderReq.setStatus(OrderStatusVO.PAY_SUCCESS.getCode());
        payOrderReq.setPayTime(payTime);
        //2.保存数据库
        orderDao.changeOrderPaySuccess(payOrderReq);

        //3.发送消息
        BaseEvent.EventMessage<PaySuccessMessageEvent.PaySuccessMessage> paySuccessMessageEventMessage = paySuccessMessageEvent.buildEventMessage(PaySuccessMessageEvent.PaySuccessMessage.builder().tradeNo(orderId).build());
        PaySuccessMessageEvent.PaySuccessMessage paySuccessMessage = paySuccessMessageEventMessage.getData();

        //旧版发送消息方式
//        eventBus.post(paySuccessMessage);

        //新版使用MQ发送消息
        eventPublisher.publisher(paySuccessMessageEvent.topic(), JSON.toJSONString(paySuccessMessage));
    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
        return orderDao.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return orderDao.queryTimeoutCloseOrderList();
    }

    @Override
    public boolean changeOrderClose(String orderId) {
        return orderDao.changeOrderClose();
    }

    @Override
    public OrderEntity queryOrderByOrderId(String orderId) {
        PayOrder payOrder = orderDao.queryOrderByOrderId(orderId);
        if(null == payOrder) return null;

        return OrderEntity.builder()
                .userId(payOrder.getUserId())
                .productId(payOrder.getProductId())
                .productName(payOrder.getProductName())
                .orderId(payOrder.getOrderId())
                .orderTime(payOrder.getOrderTime())
                .totalAmount(payOrder.getTotalAmount())
                .payAmount(payOrder.getPayAmount())
                .marketType(payOrder.getMarketType())
                .marketDeductionAmount(payOrder.getMarketDeductionAmount())
                .payUrl(payOrder.getPayUrl())
                .build();
    }

    @Override
    public void changeMarketOrderPaySuccess(String orderId) {
        orderDao.changeOrderPaySuccess(PayOrder.builder()
                        .orderId(orderId)
                        .status(OrderStatusVO.PAY_SUCCESS.getCode())
                        .build());
    }

    @Override
    public void changeOrderMarketSettlement(List<String> outTradeNoList) {
        //更新拼团结算状态
        orderDao.changeOrderMarketSettlement(outTradeNoList);

        //发送结算完成MQ通知
        outTradeNoList.forEach(outTradeNo -> {
            BaseEvent.EventMessage<PaySuccessMessageEvent.PaySuccessMessage> paySuccessMessageEventMessage = paySuccessMessageEvent.buildEventMessage(PaySuccessMessageEvent.PaySuccessMessage.builder()
                    .tradeNo(outTradeNo)
                    .build());

            PaySuccessMessageEvent.PaySuccessMessage paySuccessMessage = paySuccessMessageEventMessage.getData();

            eventPublisher.publisher(paySuccessMessageEvent.topic(), JSON.toJSONString(paySuccessMessage));
        });
    }
}
