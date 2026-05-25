package cn.idealer01.trigger.listener;

import cn.idealer01.api.dto.NotifyRequestDTO;
import cn.idealer01.domain.order.service.IOrderService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class OrderMarketSettlementListener {

    @Resource
    private IOrderService orderService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${spring.rabbitmq.config.consumer.topic_team_success.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.consumer.topic_team_success.exchange}", type = ExchangeTypes.TOPIC),
                    key = "${spring.rabbitmq.config.consumer.topic_team_success.routing_key}"
            )
    )
    public void listener(String message) {
        try {
            NotifyRequestDTO requestDTO = JSON.parseObject(message, NotifyRequestDTO.class);
            log.info("拼团回调，组队完成，结算开始 {}", com.alibaba.fastjson.JSON.toJSONString(requestDTO));
            orderService.changeOrderMarketSettlement(requestDTO.getOutTradeNoList());
            log.info("接收消息:{}", message);
        } catch (Exception e) {
            log.error("拼团回调，组队完成，结算失败 {}", message, e);
            throw e;
        }
    }
}
