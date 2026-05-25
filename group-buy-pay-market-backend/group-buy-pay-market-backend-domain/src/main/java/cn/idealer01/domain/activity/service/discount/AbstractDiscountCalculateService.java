package cn.idealer01.domain.activity.service.discount;

import cn.idealer01.domain.activity.adapter.repository.IActivityRepository;
import cn.idealer01.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.idealer01.types.enums.DiscountTypeEnum;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
public abstract class AbstractDiscountCalculateService implements IDiscountCalculateService{

    @Resource
    protected IActivityRepository repository;

    @Override
    public BigDecimal calcuate(String userId, BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount) {
        //1.人群标签过滤
        if(DiscountTypeEnum.TAG.equals(groupBuyDiscount.getDiscountType())){
            boolean isCrowedRange = fliterTagId(userId, groupBuyDiscount.getTagId());
            if(!isCrowedRange) {
                log.info("优惠计算拦截，用户不在优惠人群标签范围内：{}",userId);
                return originalPrice;
            }
        }

        //2.折扣计算
        return doCalculate(originalPrice, groupBuyDiscount);
    }

    private boolean fliterTagId(String userId, String tagId) {
        return repository.isTagCrowdRange(tagId, userId);
    }

    protected abstract BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount);

}
