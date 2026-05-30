package cn.idealer01.trigger.job;

import cn.idealer01.domain.order.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 超时关单
 * @create 2024-09-30 09:59
 */
@Slf4j
@Component()
public class TimeoutCloseOrderJob {

    @Resource
    private IOrderService orderService;

//    @Scheduled(cron = "0 0/10 * * * ?")
    public void exec() {
        try {
            log.debug("任务；超时30分钟订单关闭");
            List<String> orderIds = orderService.queryTimeoutCloseOrderList();
            if (null == orderIds || orderIds.isEmpty()) {
                log.debug("定时任务，超时30分钟订单关闭，暂无超时未支付订单");
                return;
            }
            log.info("定时任务，超时30分钟订单关闭数量：{}", orderIds.size());
            for (String orderId : orderIds) {
                boolean status = orderService.changeOrderClose(orderId);
                log.debug("定时任务，超时30分钟订单关闭 orderId: {} status：{}", orderId, status);
            }
        } catch (Exception e) {
            log.error("定时任务，超时15分钟订单关闭失败", e);
        }
    }

}
