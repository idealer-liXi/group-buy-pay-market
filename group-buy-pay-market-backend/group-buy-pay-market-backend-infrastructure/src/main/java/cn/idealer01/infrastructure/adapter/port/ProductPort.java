package cn.idealer01.infrastructure.adapter.port;

import cn.idealer01.domain.order.adapt.port.IProductPort;
import cn.idealer01.domain.order.model.entity.ProductEntity;
import cn.idealer01.domain.order.model.valobj.MarketPayDiscountEntity;
import cn.idealer01.infrastructure.dao.ISkuDao;
import cn.idealer01.infrastructure.dao.po.Sku;
import cn.idealer01.infrastructure.gateway.ProductRPC;
import cn.idealer01.infrastructure.gateway.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Slf4j
@Component
public class ProductPort implements IProductPort {

    @Value("${app.config.group-buy-market.source}")
    private String source;
    @Value("${app.config.group-buy-market.channel}")
    private String chanel;

    private final ProductRPC productRPC;
    private final LocalGroupBuyMarketPort localGroupBuyMarketPort;
    private final ISkuDao skuDao;

    public ProductPort(ProductRPC productRPC, LocalGroupBuyMarketPort localGroupBuyMarketPort, ISkuDao skuDao) {
        this.productRPC = productRPC;
        this.localGroupBuyMarketPort = localGroupBuyMarketPort;
        this.skuDao = skuDao;
    }

    @Override
    public ProductEntity queryProductByProductId(String productId) {
        Sku sku = skuDao.querySkuByGoodsId(productId);
        if (null != sku && (null == sku.getStatus() || sku.getStatus() == 0)) {
            return ProductEntity.builder()
                    .productId(sku.getGoodsId())
                    .productName(sku.getGoodsName())
                    .productDesc(sku.getGoodsName())
                    .price(sku.getOriginalPrice())
                    .build();
        }

        ProductDTO productDTO = productRPC.queryProductByProductId(productId);
        return ProductEntity.builder()
                .productId(productDTO.getProductId())
                .productName(productDTO.getProductName())
                .productDesc(productDTO.getProductDesc())
                .price(productDTO.getPrice())
                .build();
    }

    @Override
    public MarketPayDiscountEntity lockMarketPayOrder(String userId, String teamId, Long activityId, String productId, String orderId) {
        try {
            return localGroupBuyMarketPort.lockMarketPayOrder(userId, teamId, activityId, productId, orderId, source, chanel);
        } catch (Exception e) {
            log.error("营销锁单失败{}", userId, e);
            return null;
        }
    }

    @Override
    public void settlementMarketPayOrder(String userId, String orderId, Date payTime) {
        try {
            localGroupBuyMarketPort.settlementMarketPayOrder(userId, orderId, payTime, source, chanel);
        } catch (Exception e) {
            log.error("营销结算失败:{}", userId, e);
        }
    }
}
