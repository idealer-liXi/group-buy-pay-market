package cn.idealer01.domain.activity.service.trial.node;

import cn.idealer.wrench.design.framework.tree.StrategyHandler;
import cn.idealer01.domain.activity.model.entity.MarketProductEntity;
import cn.idealer01.domain.activity.model.entity.TrialBalanceEntity;
import cn.idealer01.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import cn.idealer01.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import cn.idealer.wrench.design.framework.tree.StrategyHandler;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * rootNode 一般做数据的初始操作，包括数值判断，缓存处理等内容
 */
@Service
@Slf4j
public class RootNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {

    @Resource
    private SwitchNode switchNode;

    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("拼团商品查询试算服务 rootNode--userID:{} requestParameter:{}", requestParameter.getUserId(), JSON.toJSONString(requestParameter));
        //检查参数是否存在
        if(StringUtils.isBlank(requestParameter.getUserId()) || StringUtils.isBlank(requestParameter.getGoodsId())
        || StringUtils.isBlank(requestParameter.getSource()) || StringUtils.isBlank(requestParameter.getChannel())){
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return switchNode;
    }
}
