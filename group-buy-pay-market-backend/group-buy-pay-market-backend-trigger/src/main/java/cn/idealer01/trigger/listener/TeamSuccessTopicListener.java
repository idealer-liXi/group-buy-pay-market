package cn.idealer01.trigger.listener;

import cn.idealer01.api.dto.NotifyRequestDTO;
import cn.idealer01.infrastructure.dao.IOrderDao;
import cn.idealer01.infrastructure.dao.po.PayOrder;
import cn.idealer01.trigger.websocket.UserNotificationWebSocketHandler;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TeamSuccessTopicListener {

    @Resource
    private IOrderDao orderDao;
    @Resource
    private UserNotificationWebSocketHandler userNotificationWebSocketHandler;

    public TeamSuccessTopicListener() {
    }

    public TeamSuccessTopicListener(IOrderDao orderDao, UserNotificationWebSocketHandler userNotificationWebSocketHandler) {
        this.orderDao = orderDao;
        this.userNotificationWebSocketHandler = userNotificationWebSocketHandler;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${spring.rabbitmq.config.producer.topic_team_success.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.producer.exchange}", type = ExchangeTypes.TOPIC),
                    key = "${spring.rabbitmq.config.producer.topic_team_success.routing_key}"
            )
    )
    public void listener(String message){
        log.info("接收消息(组队成功):{}", message);
        NotifyRequestDTO requestDTO = JSON.parseObject(message, NotifyRequestDTO.class);
        if (requestDTO == null || requestDTO.getOutTradeNoList() == null || requestDTO.getOutTradeNoList().isEmpty()) {
            return;
        }

        List<PayOrder> payOrderList = orderDao.queryPayOrdersByOrderIds(requestDTO.getOutTradeNoList());
        List<String> userIds = payOrderList.stream()
                .map(PayOrder::getUserId)
                .distinct()
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "GROUP_SUCCESS");
        payload.put("teamId", requestDTO.getTeamId());
        payload.put("message", "拼团已完成");
        payload.put("outTradeNoList", requestDTO.getOutTradeNoList());
        userNotificationWebSocketHandler.sendToUsers(userIds, JSON.toJSONString(payload));
    }


}
