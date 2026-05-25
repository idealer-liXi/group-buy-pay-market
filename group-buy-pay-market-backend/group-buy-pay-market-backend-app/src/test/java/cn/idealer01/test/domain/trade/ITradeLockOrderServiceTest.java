package cn.idealer01.test.domain.trade;

import cn.idealer01.domain.activity.model.entity.MarketProductEntity;
import cn.idealer01.domain.activity.model.entity.TrialBalanceEntity;
import cn.idealer01.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.idealer01.domain.activity.service.IIndexGroupBuyMarketService;
import cn.idealer01.domain.trade.model.entity.MarketPayOrderEntity;
import cn.idealer01.domain.trade.model.entity.PayActivityEntity;
import cn.idealer01.domain.trade.model.entity.PayDiscountEntity;
import cn.idealer01.domain.trade.model.entity.UserEntity;
import cn.idealer01.domain.trade.service.ITradeLockOrderService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@Slf4j
@RunWith(SpringRunner.class)
public class ITradeLockOrderServiceTest {

    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;

    @Resource
    private ITradeLockOrderService tradeOrderService;

    @Test
    public void test_lockMarketPayOrder() throws Exception{
        Long activityId = 100123L;
        String userId = "xiaofuge";
        String goodsId = "9890001";
        String source = "s01";
        String channel = "c01";
        String outTradeNo = "909000098111";


        //营销优惠试算
        TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(MarketProductEntity.builder()
                .userId(userId)
                .source(source)
                .channel(channel)
                .goodsId(goodsId)
                .activityId(activityId)
                .build());

        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = trialBalanceEntity.getGroupBuyActivityDiscountVO();

        //锁单
        MarketPayOrderEntity marketPayOrderEntity = tradeOrderService.lockMarketPayOrder(
                UserEntity.builder()
                        .userId(userId)
                        .build(),
                PayActivityEntity.builder()
                        .teamId("84164087")
                        .activityId(activityId)
                        .activityName(groupBuyActivityDiscountVO.getActivityName())
                        .startTime(groupBuyActivityDiscountVO.getStartTime())
                        .endTime(groupBuyActivityDiscountVO.getEndTime())
                        .targetCount(groupBuyActivityDiscountVO.getTarget())
                        .build(),
                PayDiscountEntity.builder()
                        .source(source)
                        .channel(channel)
                        .goodsId(goodsId)
                        .goodsName(trialBalanceEntity.getGoodsName())
                        .originalPrice(trialBalanceEntity.getOriginalPrice())
                        .deductionPrice(trialBalanceEntity.getDeductionPrice())
                        .outTradeNo(outTradeNo)
                        .build()
        );

        log.info("新的交易锁单记录:{} marketPayOrderEntity:{}", userId, JSON.toJSONString(marketPayOrderEntity));


    }

}
