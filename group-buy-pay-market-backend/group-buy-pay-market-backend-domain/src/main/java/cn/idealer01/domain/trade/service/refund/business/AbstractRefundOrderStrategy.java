package cn.idealer01.domain.trade.service.refund.business;

import cn.idealer01.domain.trade.adapter.respository.ITradeRepository;
import cn.idealer01.domain.trade.model.entity.NotifyTaskEntity;
import cn.idealer01.domain.trade.model.valobj.TeamRefundSuccess;
import cn.idealer01.domain.trade.service.ITradeTaskService;
import cn.idealer01.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import cn.idealer01.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public abstract class AbstractRefundOrderStrategy implements IRefundOrderStrategy {

    @Resource
    protected ITradeTaskService tradeTaskService;
    @Resource
    protected ThreadPoolExecutor threadPoolExecutor;
    @Resource
    protected ITradeRepository repository;

    protected void sendRefundNotifyMessage(NotifyTaskEntity notifyTask, String refundType){
        if(null != notifyTask){
            threadPoolExecutor.execute(() -> {
                Map<String, Integer> resultMap = null;
                try {
                    resultMap = tradeTaskService.execNotifyJob(notifyTask);
                    log.info("回调通知退单成功, refundType:{}, resultMap:{}", refundType, JSON.toJSONString(resultMap));
                } catch (Exception e) {
                    log.error("回调通知退单失败, refundType:{}, resultMap:{}", refundType, JSON.toJSONString(resultMap),e);
                    throw new AppException(e.getMessage());
                }
            });
        }

    }

    protected void doReverseStock(TeamRefundSuccess teamRefundSuccess, String refundType){
        log.info("退单；恢复锁单量 - 退单类型:{}, 恢复锁单库存 {} {} {}", refundType, teamRefundSuccess.getUserId(), teamRefundSuccess.getActivityId(), teamRefundSuccess.getTeamId());
        //1.生成恢复库存锁单key
        String recoveryTeamStockKey = TradeLockRuleFilterFactory.generateRecoveryTeamStockKey(teamRefundSuccess.getTeamId(), teamRefundSuccess.getActivityId());
        //2.恢复库存锁单
        repository.refund2AddRecovery(recoveryTeamStockKey, teamRefundSuccess.getOrderId());
    }

}
