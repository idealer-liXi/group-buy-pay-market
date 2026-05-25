package cn.idealer01.domain.activity.service.discount.impl;

import cn.idealer01.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.idealer01.domain.activity.service.discount.AbstractDiscountCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 指定价格
 */
@Slf4j
@Service("N")
public class NCalculateService extends AbstractDiscountCalculateService {
    @Override
    protected BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount) {
        log.info("指定折扣计算：{}", groupBuyDiscount.getDiscountType().getCode());
        //指定折扣后价格
        String marketExpr = groupBuyDiscount.getMarketExpr();
        return new BigDecimal(marketExpr);
    }
}
