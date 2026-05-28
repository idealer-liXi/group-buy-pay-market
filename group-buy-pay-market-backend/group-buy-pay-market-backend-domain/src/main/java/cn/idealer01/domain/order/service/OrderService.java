package cn.idealer01.domain.order.service;

import cn.idealer01.domain.order.adapt.port.IProductPort;
import cn.idealer01.domain.order.adapt.reposity.IOrderRepository;
import cn.idealer01.domain.order.model.aggregate.CreateOrderAggregate;
import cn.idealer01.domain.order.model.entity.OrderEntity;
import cn.idealer01.domain.order.model.entity.PayOrderEntity;
import cn.idealer01.domain.order.model.valobj.MarketPayDiscountEntity;
import cn.idealer01.domain.order.model.valobj.MarketTypeVO;
import cn.idealer01.domain.order.model.valobj.OrderStatusVO;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class OrderService extends AbstactOrderService{
    @Value("${alipay.notify-url}")
    private String notifyUrl;
    @Value("${alipay.return-url}")
    private String returnUrl;
    @Resource
    private AlipayClient alipayClient;

    protected OrderService(IOrderRepository repository, IProductPort port) {
        super(repository, port);
    }

    @Override
    protected PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount, MarketPayDiscountEntity marketPayDiscountEntity) throws AlipayApiException {
        BigDecimal payAmount = null == marketPayDiscountEntity? totalAmount : marketPayDiscountEntity.getPayPrice();

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        bizContent.put("total_amount", payAmount);
        bizContent.put("subject", productName);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        String form = alipayClient.pageExecute(request).getBody();

        PayOrderEntity payOrderEntity = new PayOrderEntity();
        payOrderEntity.setOrderId(orderId);
        payOrderEntity.setPayUrl(form);
        payOrderEntity.setOrderStatus(OrderStatusVO.PAY_WAIT);

        //设置营销信息
        payOrderEntity.setMarketType(null == marketPayDiscountEntity? MarketTypeVO.NO_MARKET.getCode() : MarketTypeVO.GROUP_BUY_MARKET.getCode());
        payOrderEntity.setMarketDeductionAmount(null == marketPayDiscountEntity? BigDecimal.ZERO : marketPayDiscountEntity.getDeductionPrice());
        payOrderEntity.setPayAmount(payAmount);

        repository.updateOrderPayInfo(payOrderEntity);

        return payOrderEntity;

    }

    @Override
    protected MarketPayDiscountEntity lockMarketPayOrder(String userId, String teamId, Long activityId, String productId, String orderId) {
        return port.lockMarketPayOrder(userId, teamId, activityId, productId, orderId);
    }

    @Override
    protected PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException {
        return doPrepayOrder(userId, productId, productName, orderId, totalAmount, null);
    }

    @Override
    public void doSaveOrder(CreateOrderAggregate orderAggregate) {
        repository.doSaveOrder(orderAggregate);
    }

    @Override
    public void changeOrderPaySuccess(String orderId, Date payTime) {
        OrderEntity orderEntity = repository.queryOrderByOrderId(orderId);
        if(null == orderEntity) return ;

        if(orderEntity.getMarketType().equals(MarketTypeVO.GROUP_BUY_MARKET.getCode())){
            //拼团支付成功
            //更新数据库
            repository.changeMarketOrderPaySuccess(orderId);
            //调用拼团活动结算完成
            port.settlementMarketPayOrder(orderEntity.getUserId(), orderId, payTime);
        }else{
            //没有参与拼团营销
            repository.changeOrderPaySuccess(orderId, payTime);
        }

    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
        return repository.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return repository.queryTimeoutCloseOrderList();
    }

    @Override
    public boolean changeOrderClose(String orderId) {
        return repository.changeOrderClose(orderId);
    }

    @Override
    public void changeOrderMarketSettlement(List<String> outTradeNoList) {
        repository.changeOrderMarketSettlement(outTradeNoList);
    }
}
