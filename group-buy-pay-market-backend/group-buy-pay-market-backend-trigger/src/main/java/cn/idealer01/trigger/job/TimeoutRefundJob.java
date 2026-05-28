package cn.idealer01.trigger.job;

import cn.idealer01.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundBehaviorEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundCommandEntity;
import cn.idealer01.domain.trade.service.ITradeRefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TimeoutRefundJob {

    @Resource
    private ITradeRefundOrderService tradeRefundOrderService;

    @Resource
    private RedissonClient redissonClient;

    @Scheduled(cron = "0 */1 * * * ?")
    public void exec() {
        //分布式锁，避免多实例重复运行
        RLock lock = redissonClient.getLock("group_buy_market_timeout_refund_job_exec");
        try{
            boolean isLocked = lock.tryLock(3, 60, TimeUnit.SECONDS);
            if (!isLocked) {
                log.info("超时退单定时任务，获取锁失败，跳过本次执行");
                return;
            }

            log.info("超时退单定时任务开始执行");

            List<UserGroupBuyOrderDetailEntity> timeoutOrderList = new ArrayList<>();
            List<UserGroupBuyOrderDetailEntity> timeoutUnpaidOrderList = tradeRefundOrderService.queryTimeoutUnpaidOrderList();
            if (null != timeoutUnpaidOrderList && !timeoutUnpaidOrderList.isEmpty()) {
                timeoutOrderList.addAll(timeoutUnpaidOrderList);
            }
            List<UserGroupBuyOrderDetailEntity> timeoutPaidUnformedOrderList = tradeRefundOrderService.queryTimeoutPaidUnformedOrderList();
            if (null != timeoutPaidUnformedOrderList && !timeoutPaidUnformedOrderList.isEmpty()) {
                timeoutOrderList.addAll(timeoutPaidUnformedOrderList);
            }
            if(timeoutOrderList.isEmpty()){
                log.info("超时退单定时任务，未发现超时订单");
                return;
            }

            log.info("超时退单定时任务，发现超时订单数量：{}", timeoutOrderList.size());

            //统计 超时关单 成功次数与失败次数
            int successCount = 0;
            int failCount = 0;


            for (UserGroupBuyOrderDetailEntity orderDetail : timeoutOrderList) {
                try{
                    TradeRefundCommandEntity tradeRefundCommandEntity = TradeRefundCommandEntity.builder()
                            .userId(orderDetail.getUserId())
                            .outTradeNo(orderDetail.getOutTradeNo())
                            .source(orderDetail.getSource())
                            .channel(orderDetail.getChannel())
                            .build();

                    //执行关单
                    tradeRefundOrderService.refundOrder(tradeRefundCommandEntity);
                    successCount ++;

                    log.info("超时订单退单成功，用户ID：{}，交易单号：{}", orderDetail.getUserId(), orderDetail.getOutTradeNo());
                }catch (Exception e){
                    failCount ++;
                    log.error("超时订单退单失败，用户ID：{}，交易单号：{}，错误信息：{}",
                            orderDetail.getUserId(), orderDetail.getOutTradeNo(), e.getMessage(), e);
                }
            }

            log.info("超时退单定时任务执行完成，成功：{}，失败：{}", successCount, failCount);
        }catch (Exception e){
            log.error("超时退单定时任务执行异常", e);
        }finally {
            if(lock.isLocked() && lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }

    }

}
