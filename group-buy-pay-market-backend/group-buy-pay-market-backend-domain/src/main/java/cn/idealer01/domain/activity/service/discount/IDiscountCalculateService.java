package cn.idealer01.domain.activity.service.discount;

import cn.idealer01.domain.activity.model.valobj.GroupBuyActivityDiscountVO;

import java.math.BigDecimal;

public interface IDiscountCalculateService {

    BigDecimal calcuate(String userId, BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount discount);
}
