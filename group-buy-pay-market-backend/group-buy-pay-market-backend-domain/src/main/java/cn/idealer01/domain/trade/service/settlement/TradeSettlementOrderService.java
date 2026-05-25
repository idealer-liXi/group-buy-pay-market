package cn.idealer01.domain.trade.service.settlement;

import cn.idealer.wrench.design.framework.link.model2.chain.BusinessLinkedList;
import cn.idealer01.domain.trade.adapter.port.ITradePort;
import cn.idealer01.domain.trade.adapter.respository.ITradeRepository;
import cn.idealer01.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import cn.idealer01.domain.trade.model.entity.*;
import cn.idealer01.domain.trade.service.ITradeSettlementOrderService;
import cn.idealer01.domain.trade.service.ITradeTaskService;
import cn.idealer01.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import cn.idealer01.types.enums.NotifyTaskHTTPEnumVO;
import cn.idealer01.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class TradeSettlementOrderService implements ITradeSettlementOrderService {
    @Resource
    private ITradeRepository repository;
    @Resource
    private ITradePort port;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private ITradeTaskService tradeTaskService;

    @Resource
    private BusinessLinkedList<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> tradeSettlementRuleFilter;

    @Override
    public TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception {
        log.info("拼团交易-支付订单结算:{} outTradeNo:{}", tradePaySuccessEntity.getUserId(), tradePaySuccessEntity.getOutTradeNo());
        //1.结算规则过滤
        TradeSettlementRuleFilterBackEntity tradeSettlementRuleFilterBackEntity = tradeSettlementRuleFilter.apply(
                TradeSettlementRuleCommandEntity.builder()
                        .source(tradePaySuccessEntity.getSource())
                        .channel(tradePaySuccessEntity.getChannel())
                        .userId(tradePaySuccessEntity.getUserId())
                        .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                        .outTradeTime(tradePaySuccessEntity.getOutTradeTime())
                        .build(),
                new TradeSettlementRuleFilterFactory.DynamicContext()
        );
        String teamId = tradeSettlementRuleFilterBackEntity.getTeamId();

        //2.查询组团信息
        GroupBuyTeamEntity groupBuyTeamEntity = GroupBuyTeamEntity.builder()
                .teamId(tradeSettlementRuleFilterBackEntity.getTeamId())
                .activityId(tradeSettlementRuleFilterBackEntity.getActivityId())
                .targetCount(tradeSettlementRuleFilterBackEntity.getTargetCount())
                .completeCount(tradeSettlementRuleFilterBackEntity.getCompleteCount())
                .lockCount(tradeSettlementRuleFilterBackEntity.getLockCount())
                .status(tradeSettlementRuleFilterBackEntity.getStatus())
                .validStartTime(tradeSettlementRuleFilterBackEntity.getValidStartTime())
                .validEndTime(tradeSettlementRuleFilterBackEntity.getValidEndTime())
                .notifyConfigVO(tradeSettlementRuleFilterBackEntity.getNotifyConfigVO())
                .build();

        //3.构建聚合对象
        GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate = GroupBuyTeamSettlementAggregate.builder()
                .userEntity(UserEntity.builder().userId(tradePaySuccessEntity.getUserId()).build())
                .groupBuyTeamEntity(groupBuyTeamEntity)
                .tradePaySuccessEntity(tradePaySuccessEntity)
                .build();

        //4.拼团交易结算
        NotifyTaskEntity notifyTaskEntity = repository.settlementMarketPayOrder(groupBuyTeamSettlementAggregate);

        //5.组队拼团完结,异步回调通知
        if(null != notifyTaskEntity){
            threadPoolExecutor.execute(() -> {
                Map<String, Integer> notifyResultMap = null;
                try{
                    notifyResultMap = tradeTaskService.execNotifyJob(notifyTaskEntity);
                    log.info("回调通知拼团完结 result:{}", JSON.toJSONString(notifyResultMap));
                }catch (Exception e){
                    log.error("回调通知拼团完结失败 result:{}", JSON.toJSONString(notifyResultMap), e);
                    throw new AppException(e.getMessage());
                }
            });
        }

        //6.返回结算信息
        return TradePaySettlementEntity.builder()
                .userId(tradePaySuccessEntity.getUserId())
                .channel(tradePaySuccessEntity.getChannel())
                .source(tradePaySuccessEntity.getSource())
                .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                .teamId(teamId)
                .activityId(groupBuyTeamEntity.getActivityId())
                .build();
    }


}
