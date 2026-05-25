package cn.idealer01.domain.order.adapt.port;

import cn.idealer01.domain.order.model.entity.ProductEntity;
import cn.idealer01.domain.order.model.valobj.MarketPayDiscountEntity;

import java.util.Date;

public interface IProductPort {
    ProductEntity queryProductByProductId(String productId);


    MarketPayDiscountEntity lockMarketPayOrder(String userId, String teamId, Long activityId, String productId, String orderId);

    void settlementMarketPayOrder(String userId, String orderId, Date payTime);
}
