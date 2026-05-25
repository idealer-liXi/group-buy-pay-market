package cn.idealer01.domain.trade.service.lock.filter;

import cn.idealer01.domain.trade.adapter.respository.ITradeRepository;
import cn.idealer01.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.idealer01.domain.trade.model.entity.TradeLockRuleCommandEntity;
import cn.idealer01.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import cn.idealer01.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import cn.idealer.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class TeamStockOccupyRuleFilter implements ILogicHandler<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> {
    @Resource
    private ITradeRepository repository;

    @Override
    public TradeLockRuleFilterBackEntity apply(TradeLockRuleCommandEntity requestParameter, TradeLockRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("交易规则过滤-组队库存校验{} activityId:{}", requestParameter.getUserId(), requestParameter.getActivityId());

        //teamId为空，表明为首次开团，不做首次开团目标库存限制
        String teamId = requestParameter.getTeamId();
        if(StringUtils.isBlank(teamId)){
            return TradeLockRuleFilterBackEntity.builder()
                    .userTakeOrderCount(dynamicContext.getUserTakeOrderCount())
                    .build();
        }

        //通过redis进行锁单，缓解数据库压力
        GroupBuyActivityEntity groupBuyActivity = dynamicContext.getGroupBuyActivity();
        Integer target = groupBuyActivity.getTarget();
        Integer validTime = groupBuyActivity.getValidTime();
        String teamStockKey = dynamicContext.generateTeamStockKey(teamId);
        String recoveryTeamStockKey = dynamicContext.generateRecoveryTeamStockKey(teamId);

        //在redis中抢占锁
        boolean status = repository.occupyTeamStock(teamStockKey, recoveryTeamStockKey, target, validTime);

        if(!status){
            //在redis中抢占锁失败，直接锁单失败
            log.warn("交易规则过滤-组队库存校验{} activityId:{} 抢占失败:{}", requestParameter.getUserId(), requestParameter.getActivityId(), teamStockKey);
            throw new AppException(ResponseCode.E0008);
        }

        //redis锁单成功，但是还未在数据库中操作，在数据库中可能操作失败，那么抢占的拼团名额需要恢复
        return TradeLockRuleFilterBackEntity.builder()
                .userTakeOrderCount(dynamicContext.getUserTakeOrderCount())
                //用户恢复抢占的redis名额
                .recoveryTeamStockKey(recoveryTeamStockKey)
                .build();
    }


}
