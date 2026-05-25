package cn.idealer01.domain.activity.service.trial.factory;

import cn.idealer.wrench.design.framework.tree.DynamicContext;
import cn.idealer01.domain.activity.model.entity.MarketProductEntity;
import cn.idealer01.domain.activity.model.entity.TrialBalanceEntity;
import cn.idealer01.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.idealer01.domain.activity.model.valobj.SkuVO;
import cn.idealer01.domain.activity.service.trial.node.RootNode;
import cn.idealer.wrench.design.framework.tree.StrategyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DefaultActivityStrategyFactory {
    private final RootNode rootNode;

    public DefaultActivityStrategyFactory(RootNode rootNode){
        this.rootNode = rootNode;
    }

    public StrategyHandler<MarketProductEntity, DynamicContext, TrialBalanceEntity> strategyHandler(){
        return rootNode;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DynamicContext extends cn.idealer.wrench.design.framework.tree.DynamicContext {

        private GroupBuyActivityDiscountVO groupBuyActivityDiscountVO;

        private SkuVO skuVO;
        private BigDecimal DeductionPrice;
        private BigDecimal payPrice;

        private boolean visible;
        private boolean enable;
    }
}
