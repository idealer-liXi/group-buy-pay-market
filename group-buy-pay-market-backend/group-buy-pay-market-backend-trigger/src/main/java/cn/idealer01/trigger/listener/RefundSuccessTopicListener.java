package cn.idealer01.trigger.listener;

import cn.idealer01.domain.trade.model.valobj.TeamRefundSuccess;
import cn.idealer01.domain.trade.service.ITradeRefundOrderService;
import cn.idealer01.trigger.websocket.UserNotificationWebSocketHandler;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RefundSuccessTopicListener {

    @Resource
    private ITradeRefundOrderService tradeRefundOrderService;
    @Resource
    private UserNotificationWebSocketHandler userNotificationWebSocketHandler;

    public RefundSuccessTopicListener() {
    }

    public RefundSuccessTopicListener(ITradeRefundOrderService tradeRefundOrderService, UserNotificationWebSocketHandler userNotificationWebSocketHandler) {
        this.tradeRefundOrderService = tradeRefundOrderService;
        this.userNotificationWebSocketHandler = userNotificationWebSocketHandler;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${spring.rabbitmq.config.producer.topic_team_refund.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.producer.exchange}", type = ExchangeTypes.TOPIC),
                    key = "${spring.rabbitmq.config.producer.topic_team_refund.routing_key}"
            )
    )
    public void listener(String message){
        log.info("接收消息(退单成功) - 恢复拼团队伍锁单量 {}", message);

        TeamRefundSuccess teamRefundSuccess = JSON.parseObject(message, TeamRefundSuccess.class);

        try{
            tradeRefundOrderService.restoreTeamLockStock(teamRefundSuccess);
            pushRefundNotification(teamRefundSuccess);
        }catch (Exception e){
            log.error("接收消息(退单成功) - 恢复拼团队伍锁单量失败 {}", message, e);
            //抛出异常，MQ消息会进行重试发布
            throw new RuntimeException(e);
        }


    }

    private void pushRefundNotification(TeamRefundSuccess teamRefundSuccess) {
        if (teamRefundSuccess == null || StringUtils.isBlank(teamRefundSuccess.getUserId())) {
            return;
        }
        String type = isGroupFail(teamRefundSuccess.getType()) ? "GROUP_FAIL" : "REFUND_SUCCESS";
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);
        payload.put("teamId", teamRefundSuccess.getTeamId());
        payload.put("orderId", teamRefundSuccess.getOutTradeNo());
        payload.put("message", "GROUP_FAIL".equals(type) ? "拼团失败，订单已退单" : "退单成功");
        userNotificationWebSocketHandler.sendToUsers(Collections.singleton(teamRefundSuccess.getUserId()), JSON.toJSONString(payload));
    }

    private boolean isGroupFail(String refundType) {
        return "paid_unformed".equals(refundType)
                || "unpaid_unlock".equals(refundType)
                || "PAID_UNFORMED".equals(refundType)
                || "UNPAID_UNLOCK".equals(refundType);
    }
}
