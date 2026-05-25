package cn.idealer01.domain.activity.service.trial.node;

import cn.idealer01.domain.activity.model.entity.MarketProductEntity;
import cn.idealer01.domain.activity.model.entity.TrialBalanceEntity;
import cn.idealer01.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.idealer01.domain.activity.model.valobj.SkuVO;
import cn.idealer01.domain.activity.service.discount.AbstractDiscountCalculateService;
import cn.idealer01.domain.activity.service.discount.IDiscountCalculateService;
import cn.idealer01.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import cn.idealer01.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import cn.idealer01.domain.activity.service.trial.thread.QueryGroupBuyActivityVOThreadTask;
import cn.idealer01.domain.activity.service.trial.thread.QuerySkuVOFromDBThreadTask;
import cn.idealer.wrench.design.framework.tree.StrategyHandler;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
public class MarketNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private ErrorNode errorNode;
    @Resource
    private TagNode tagNode;
    @Resource
    private Map<String, AbstractDiscountCalculateService> discountCalculateServiceMap;

    @Override
    protected void multiThread(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        //1.异步查询活动配置
        QueryGroupBuyActivityVOThreadTask queryGroupBuyActivityVOThreadTask = new QueryGroupBuyActivityVOThreadTask(requestParameter.getActivityId(), requestParameter.getSource(), requestParameter.getChannel(), requestParameter.getGoodsId(),activityRepository);
        FutureTask<GroupBuyActivityDiscountVO> groupBuyActivityVOThreadTaskFutureTask = new FutureTask<>(queryGroupBuyActivityVOThreadTask);
        threadPoolExecutor.execute(groupBuyActivityVOThreadTaskFutureTask);

        //2.异步查询商品信息
        QuerySkuVOFromDBThreadTask querySkuVOFromDBThreadTask = new QuerySkuVOFromDBThreadTask(requestParameter.getGoodsId(), activityRepository);
        FutureTask<SkuVO> skuVOFutureTask = new FutureTask<>(querySkuVOFromDBThreadTask);
        threadPoolExecutor.execute(skuVOFutureTask);

        // 将查询信息写入上下文 - 对于一些复杂场景，获取数据的操作，有时候会在下N个节点获取，这样前置查询数据，可以提高接口响应效率
        dynamicContext.setGroupBuyActivityDiscountVO(groupBuyActivityVOThreadTaskFutureTask.get(timeout, TimeUnit.MINUTES));
        dynamicContext.setSkuVO(skuVOFutureTask.get(timeout, TimeUnit.MINUTES));
    }

    @Override
    protected TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("拼团商品查询试算服务-MarketNode userId:{} requestParameter:{}", requestParameter.getUserId(), JSON.toJSONString(requestParameter));

        //获取上下文数据
        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = dynamicContext.getGroupBuyActivityDiscountVO();
        if(null == groupBuyActivityDiscountVO){
            return router(requestParameter, dynamicContext);
        }

        GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount = groupBuyActivityDiscountVO.getGroupBuyDiscount();
        SkuVO skuVO = dynamicContext.getSkuVO();
        if(null == groupBuyDiscount || null == skuVO){
            return router(requestParameter, dynamicContext);
        }

        //优惠试算
        IDiscountCalculateService discountCalculateService = discountCalculateServiceMap.get(groupBuyDiscount.getMarketPlan());
        if(null == discountCalculateService){
            log.info("不存在{}类型的折扣计算服务，支持类型为{}", groupBuyDiscount.getMarketPlan(), JSON.toJSONString(discountCalculateServiceMap.keySet()));
            throw new AppException(ResponseCode.E0001.getCode(), ResponseCode.E0001.getInfo());
        }

        //计算折扣价格
        BigDecimal payPrice = discountCalculateService.calcuate(requestParameter.getUserId(), skuVO.getOriginalPrice(), groupBuyDiscount);
        dynamicContext.setDeductionPrice(skuVO.getOriginalPrice().subtract(payPrice));
        dynamicContext.setPayPrice(payPrice);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        if(null == dynamicContext.getGroupBuyActivityDiscountVO() || null == dynamicContext.getSkuVO() || null == dynamicContext.getDeductionPrice()){
            return errorNode;
        }

        return tagNode;
    }
}
