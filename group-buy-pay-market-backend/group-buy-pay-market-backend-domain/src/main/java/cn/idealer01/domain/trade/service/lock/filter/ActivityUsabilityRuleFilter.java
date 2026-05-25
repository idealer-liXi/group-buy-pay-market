package cn.idealer01.domain.trade.service.lock.filter;

import cn.idealer.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.idealer01.domain.trade.adapter.respository.ITradeRepository;
import cn.idealer01.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.idealer01.domain.trade.model.entity.TradeLockRuleCommandEntity;
import cn.idealer01.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import cn.idealer01.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import cn.idealer.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.idealer01.types.enums.ActivityStatusEnumVO;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class ActivityUsabilityRuleFilter implements ILogicHandler<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

    @Override
    public TradeLockRuleFilterBackEntity apply(TradeLockRuleCommandEntity requestParameter, TradeLockRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("交易规则过滤--活动可行性校验 userId:{}, activityId:{}",requestParameter.getUserId(), requestParameter.getActivityId());

        // 查询拼团活动
        GroupBuyActivityEntity groupBuyActivityEntity = repository.queryGroupBuyActivityEntityByActivityId(requestParameter.getActivityId());

        // 校验：活动状态
        if(!ActivityStatusEnumVO.EFFECTIVE.equals(groupBuyActivityEntity.getStatus())){
            log.info("活动的可行性校验，非生效状态 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0101);
        }

        // 校验：活动时间
        Date currentTime = new Date();
        if(currentTime.before(groupBuyActivityEntity.getStartTime()) || currentTime.after(groupBuyActivityEntity.getEndTime())){
            log.info("活动的可行性校验，非可参与时间 activity:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0102);
        }

        //写入上下文
        dynamicContext.setGroupBuyActivity(groupBuyActivityEntity);

        return next(requestParameter, dynamicContext);
    }

}
