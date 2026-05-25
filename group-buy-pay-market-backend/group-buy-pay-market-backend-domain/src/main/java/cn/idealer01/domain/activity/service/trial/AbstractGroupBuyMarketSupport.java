package cn.idealer01.domain.activity.service.trial;

import cn.idealer.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import cn.idealer01.domain.activity.adapter.repository.IActivityRepository;
import cn.idealer01.domain.activity.model.entity.MarketProductEntity;
import cn.idealer01.domain.activity.model.entity.TrialBalanceEntity;
import cn.idealer01.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import cn.idealer.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
public abstract class AbstractGroupBuyMarketSupport<m, d, t> extends AbstractMultiThreadStrategyRouter<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {

    protected long timeout = 5000;

    @Resource
    protected IActivityRepository activityRepository;

    @Override
    protected void multiThread(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {

    }
}
