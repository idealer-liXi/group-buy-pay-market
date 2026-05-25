package cn.idealer01.domain.activity.service.trial.node;

import cn.idealer01.domain.activity.model.entity.MarketProductEntity;
import cn.idealer01.domain.activity.model.entity.TrialBalanceEntity;
import cn.idealer01.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import cn.idealer01.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import cn.idealer.wrench.design.framework.tree.StrategyHandler;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class SwitchNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {
    @Resource
    private MarketNode marketNode;

    @Override
    protected TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("拼团商品查询试算服务——SwitchNode userId:{} requestParameter:{}", requestParameter.getUserId(), requestParameter);

        String userId = requestParameter.getUserId();

        //判断是否降级
        if(activityRepository.downgradeSwitch()){
            log.info("拼团活动降级拦截{}", userId);
            throw new AppException(ResponseCode.E0003.getCode(), ResponseCode.E0003.getInfo());
        }

        //判断是否在切量范围
        if(!activityRepository.cutRange(userId)){
            log.info("拼团活动切量拦截{}", userId);
            throw new AppException(ResponseCode.E0004.getCode(), ResponseCode.E0004.getInfo());
        }

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return marketNode;
    }
}
