package cn.idealer01.domain.order.adapt.reposity;

import cn.idealer01.domain.order.model.aggregate.CreateOrderAggregate;
import cn.idealer01.domain.order.model.entity.OrderEntity;
import cn.idealer01.domain.order.model.entity.PayOrderEntity;
import cn.idealer01.domain.order.model.entity.ShopCartEntity;

import java.util.Date;
import java.util.List;

public interface IOrderRepository {
    OrderEntity queryUnPayOrder(ShopCartEntity shopCartEntity);

    void doSaveOrder(CreateOrderAggregate orderAggregate);

    void updateOrderPayInfo(PayOrderEntity payOrderEntity);

    void changeOrderPaySuccess(String orderId, Date payDate);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose(String orderId);

    OrderEntity queryOrderByOrderId(String orderId);

    void changeMarketOrderPaySuccess(String orderId);

    void changeOrderMarketSettlement(List<String> outTradeNoList);
}
