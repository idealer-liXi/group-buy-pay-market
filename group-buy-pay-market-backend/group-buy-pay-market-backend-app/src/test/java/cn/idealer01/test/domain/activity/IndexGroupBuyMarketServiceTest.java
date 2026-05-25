package cn.idealer01.test.domain.activity;

import cn.idealer01.domain.activity.model.entity.MarketProductEntity;
import cn.idealer01.domain.activity.model.entity.TrialBalanceEntity;
import cn.idealer01.domain.activity.service.IIndexGroupBuyMarketService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class IndexGroupBuyMarketServiceTest {

    @Resource
    private IIndexGroupBuyMarketService service;

    @Test
    public void test1() throws Exception {
        MarketProductEntity marketProductEntity = MarketProductEntity.builder()
                .userId("xfg01")
                .goodsId("9890001")
                .channel("c01")
                .source("s01")
                .build();

        TrialBalanceEntity trialBalanceEntity = service.indexMarketTrial(marketProductEntity);
        log.info("请求参数：{}", JSON.toJSONString(marketProductEntity));
        log.info("返回参数：{}", JSON.toJSONString(trialBalanceEntity));

    }

    @Test
    public void test_indexMarketTrial_errornode() throws Exception {
        MarketProductEntity marketProductEntity = MarketProductEntity.builder()
                .userId("xiaofuge")
                .goodsId("9890002")
                .channel("c01")
                .source("s01")
                .build();

        TrialBalanceEntity trialBalanceEntity = service.indexMarketTrial(marketProductEntity);
        log.info("请求参数：{}", JSON.toJSONString(marketProductEntity));
        log.info("返回参数：{}", JSON.toJSONString(trialBalanceEntity));
    }


    @Test
    public void test_indexMarketTrial_no_tag() throws Exception {
        MarketProductEntity marketProductEntity = MarketProductEntity.builder()
                .userId("xiaofuge")
                .goodsId("9890001")
                .channel("c01")
                .source("s01")
                .build();

        TrialBalanceEntity trialBalanceEntity = service.indexMarketTrial(marketProductEntity);
        log.info("请求参数：{}", JSON.toJSONString(marketProductEntity));
        log.info("返回参数：{}", JSON.toJSONString(trialBalanceEntity));
    }
}
