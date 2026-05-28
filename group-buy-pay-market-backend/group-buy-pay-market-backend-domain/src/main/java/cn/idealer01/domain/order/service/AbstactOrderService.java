package cn.idealer01.domain.order.service;

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
import com.alipay.api.AlipayApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@Slf4j
public abstract class AbstactOrderService implements IOrderService{
    protected final IOrderRepository repository;
    protected final IProductPort port;

    protected AbstactOrderService(IOrderRepository repository, IProductPort port) {
        this.repository = repository;
        this.port = port;
    }


    @Override
    public PayOrderEntity createOrder(ShopCartEntity shopCartEntity) throws Exception {
        // 1. 查询当前用户是否存在掉单和未支付订单
        OrderEntity unpaidOrderEntity = repository.queryUnPayOrder(shopCartEntity);
        Integer requestedMarketType = null == shopCartEntity.getMarketTypeVO() ? null : shopCartEntity.getMarketTypeVO().getCode();
        if (null != unpaidOrderEntity && null != requestedMarketType && !Objects.equals(unpaidOrderEntity.getMarketType(), requestedMarketType)) {
            log.info("创建订单-忽略不同营销类型未支付订单。userId:{} productId:{} orderId:{} requestedMarketType:{} existedMarketType:{}",
                    shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getOrderId(), requestedMarketType, unpaidOrderEntity.getMarketType());
            unpaidOrderEntity = null;
        }

        if (null != unpaidOrderEntity && OrderStatusVO.PAY_WAIT.equals(unpaidOrderEntity.getOrderStatusVO())) {
            log.info("创建订单-存在，已存在未支付订单。userId:{} productId:{} orderId:{}", shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getOrderId());
            //当前订单已经在数据库创建，并且已经在支付宝生成收款单，直接返回
            return PayOrderEntity.builder()
                    .orderId(unpaidOrderEntity.getOrderId())
                    .payUrl(unpaidOrderEntity.getPayUrl())
                    .build();
        } else if (null != unpaidOrderEntity && OrderStatusVO.CREATE.equals(unpaidOrderEntity.getOrderStatusVO())) {
            log.info("创建订单-存在，存在未创建支付单，userId:{} productId:{} orderId:{}", shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getOrderId());
            //当前订单已经在数据库创建，但是掉单，即未在支付宝生成收款码
            //重新获取支付宝付款页面
            Integer marketType = unpaidOrderEntity.getMarketType();
            BigDecimal marketDeductionAmount = unpaidOrderEntity.getMarketDeductionAmount();

            PayOrderEntity payOrderEntity = null;

            if(MarketTypeVO.GROUP_BUY_MARKET.getCode().equals(unpaidOrderEntity.getMarketType()) && null == marketDeductionAmount){
                //参与拼团营销但是没有计算出拼团后金额
                MarketPayDiscountEntity marketPayDiscountEntity = this.lockMarketPayOrder(shopCartEntity.getUserId(),
                        shopCartEntity.getTeamId(),
                        shopCartEntity.getActivityId(),
                        shopCartEntity.getProductId(),
                        unpaidOrderEntity.getOrderId());

                payOrderEntity = doPrepayOrder(shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getProductName(), unpaidOrderEntity.getOrderId(), unpaidOrderEntity.getTotalAmount(), marketPayDiscountEntity);
            }else if(MarketTypeVO.GROUP_BUY_MARKET.getCode().equals(unpaidOrderEntity.getMarketType())){
                //参与拼团营销并且已经计算出拼团后金额
                payOrderEntity = doPrepayOrder(shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getProductName(), unpaidOrderEntity.getOrderId(), unpaidOrderEntity.getPayAmount());
            }else{
                payOrderEntity = doPrepayOrder(shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getProductName(), unpaidOrderEntity.getOrderId(), unpaidOrderEntity.getTotalAmount());
            }


            return PayOrderEntity.builder()
                    .orderId(payOrderEntity.getOrderId())
                    .payUrl(payOrderEntity.getPayUrl())
                    .build();
        }

        //查询商品信息
        ProductEntity productEntity = port.queryProductByProductId(shopCartEntity.getProductId());
        //封装商品信息到订单信息
        OrderEntity orderEntity = CreateOrderAggregate.buildOrderEntity(productEntity.getProductId(), productEntity.getProductName(), requestedMarketType);
        //创建集成对象
        CreateOrderAggregate orderAggregate = CreateOrderAggregate.builder()
                .userId(shopCartEntity.getUserId())
                .productEntity(productEntity)
                .orderEntity(orderEntity)
                .build();

        //把订单保存到数据库
        this.doSaveOrder(orderAggregate);

        //发起营销锁单
        MarketPayDiscountEntity marketPayDiscountEntity = null;
        if(MarketTypeVO.GROUP_BUY_MARKET.equals(shopCartEntity.getMarketTypeVO())){
            marketPayDiscountEntity = this.lockMarketPayOrder(shopCartEntity.getUserId(),
                    shopCartEntity.getTeamId(),
                    shopCartEntity.getActivityId(),
                    shopCartEntity.getProductId(),
                    orderEntity.getOrderId());
        }

        //创建支付订单
        PayOrderEntity payOrderEntity = doPrepayOrder(shopCartEntity.getUserId(),
                shopCartEntity.getProductId(),
                productEntity.getProductName(),
                orderEntity.getOrderId(),
                productEntity.getPrice(),
                marketPayDiscountEntity);

        log.info("订单创建完成，生成支付单：userId:{}, productId:{}, payUrl:{}", shopCartEntity.getUserId(), productEntity.getProductId(), payOrderEntity.getPayUrl());

        return PayOrderEntity.builder()
                .orderId(orderEntity.getOrderId())
                .payUrl(payOrderEntity.getPayUrl())
                .build();
    }

    protected abstract PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount, MarketPayDiscountEntity marketPayDiscountEntity) throws AlipayApiException;

    protected abstract MarketPayDiscountEntity lockMarketPayOrder(String userId, String teamId, Long activityId, String productId, String orderId);

    protected abstract PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException;

    protected abstract void doSaveOrder(CreateOrderAggregate orderAggregate);
}
