package cn.idealer01.infrastructure.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.config.producer.topic_order_pay_success.exchange}")
    private String exchangeName;

    @Value("${spring.rabbitmq.config.producer.exchange}")
    private String groupBuyExchangeName;

    public void publisher(String routingKey, String message){
        try{
            rabbitTemplate.convertAndSend(resolveExchange(routingKey), routingKey, message, m -> {
                m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return m;
            });
        }catch (Exception e){
            log.error("发送MQ消息失败 routingKey:{} message:{}", routingKey, message, e);
            throw e;
        }

    }

    public void publish(String routingKey, String message) {
        publisher(routingKey, message);
    }

    private String resolveExchange(String routingKey) {
        if ("topic.team_success".equals(routingKey) || "topic.team_refund".equals(routingKey)) {
            return groupBuyExchangeName;
        }
        return exchangeName;
    }

}
