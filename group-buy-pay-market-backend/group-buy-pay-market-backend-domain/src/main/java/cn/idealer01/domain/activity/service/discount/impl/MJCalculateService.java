package cn.idealer01.domain.activity.service.discount.impl;

import cn.idealer01.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.idealer01.domain.activity.service.discount.AbstractDiscountCalculateService;
import cn.idealer01.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

/**
 * 满减
 */
@Slf4j
@Service("MJ")
public class MJCalculateService extends AbstractDiscountCalculateService {
    @Override
    protected BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount) {
        log.info("满减折扣计算：{}", groupBuyDiscount.getDiscountType().getCode());

        //分解折扣表达式 例：100,10 为满100减10
        String marketExpr = groupBuyDiscount.getMarketExpr();
        String[] split = marketExpr.split(Constants.SPLIT);
        BigDecimal x = new BigDecimal(split[0].trim());
        BigDecimal y = new BigDecimal(split[1].trim());

        //未满
        if(originalPrice.compareTo(x) < 0){
            return originalPrice;
        }

        //已满减去折扣
        BigDecimal deductionPrice = originalPrice.subtract(y);
        //折扣为非正数，最低支付0.01
        if(deductionPrice.compareTo(BigDecimal.ZERO) <= 0){
            return new BigDecimal("0.01");
        }

        return deductionPrice;
    }
}
