package cn.idealer01.infrastructure.dao;

import cn.idealer01.domain.order.model.entity.OrderEntity;
import cn.idealer01.infrastructure.dao.po.PayOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IOrderDao {

    void insert(PayOrder payOrder);

    PayOrder queryUnPayOrder(PayOrder payOrder);

    void updateOrderPayInfo(PayOrder payOrderReq);

    void changeOrderPaySuccess(PayOrder payOrderReq);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose();

    PayOrder queryOrderByOrderId(String orderId);

    void changeOrderMarketSettlement(@Param("outTradeNoList") List<String> outTradeNoList);

    void changeOrderDealDone(String tradeNo);
}
