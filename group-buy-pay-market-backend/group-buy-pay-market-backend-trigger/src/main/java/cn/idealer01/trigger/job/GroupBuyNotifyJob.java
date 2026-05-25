package cn.idealer01.trigger.job;

import cn.idealer01.domain.trade.service.ITradeSettlementOrderService;
import cn.idealer01.domain.trade.service.ITradeTaskService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GroupBuyNotifyJob {

    @Resource
    private ITradeTaskService tradeTaskService;

    @Resource
    private RedissonClient redissonClient;

//    @Scheduled(cron = "0/15 * * * * ?")
    public void exec(){
        //独占锁：分布式环境下多个服务通知抢夺的话会造成排队
        RLock lock = redissonClient.getLock("group_buy_market_notify_job_exec");

        try{
            boolean isLocked = lock.tryLock(3, 0, TimeUnit.SECONDS);
            if(!isLocked) return;

            Map<String, Integer> notifyResultMap = tradeTaskService.execNotifyJob();
            log.info("定时任务， 回调通知拼团完结 resultMap:{}", JSON.toJSONString(notifyResultMap));
        }catch (Exception e){
            log.error("定时任务，回调通知拼团完结失败", e);
        } finally {
            if(lock.isLocked() && lock.isHeldByCurrentThread()){
                lock.unlock();
            }

        }
    }

}
