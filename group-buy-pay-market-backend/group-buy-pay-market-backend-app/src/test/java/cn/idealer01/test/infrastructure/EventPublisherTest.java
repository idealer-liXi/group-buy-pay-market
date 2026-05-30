package cn.idealer01.test.infrastructure;

import cn.idealer01.infrastructure.event.EventPublisher;
import org.junit.Test;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class EventPublisherTest {

    @Test
    public void publisher_sendsOrderPaySuccessToPayMallExchange() throws Exception {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        EventPublisher publisher = publisher(rabbitTemplate);

        publisher.publisher("topic.order_pay_success", "{}");

        verify(rabbitTemplate).convertAndSend(eq("s_pay_mall_exchange"), eq("topic.order_pay_success"), eq("{}"), any(MessagePostProcessor.class));
    }

    @Test
    public void publisher_sendsTeamSuccessToGroupBuyExchange() throws Exception {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        EventPublisher publisher = publisher(rabbitTemplate);

        publisher.publisher("topic.team_success", "{}");

        verify(rabbitTemplate).convertAndSend(eq("group_buy_market_exchange"), eq("topic.team_success"), eq("{}"), any(MessagePostProcessor.class));
    }

    @Test
    public void publisher_sendsTeamRefundToGroupBuyExchange() throws Exception {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        EventPublisher publisher = publisher(rabbitTemplate);

        publisher.publisher("topic.team_refund", "{}");

        verify(rabbitTemplate).convertAndSend(eq("group_buy_market_exchange"), eq("topic.team_refund"), eq("{}"), any(MessagePostProcessor.class));
    }

    private EventPublisher publisher(RabbitTemplate rabbitTemplate) throws Exception {
        EventPublisher publisher = new EventPublisher();
        setField(publisher, "rabbitTemplate", rabbitTemplate);
        setField(publisher, "exchangeName", "s_pay_mall_exchange");
        setFieldIfPresent(publisher, "groupBuyExchangeName", "group_buy_market_exchange");
        return publisher;
    }

    private void setFieldIfPresent(Object target, String fieldName, Object value) throws Exception {
        try {
            setField(target, fieldName, value);
        } catch (NoSuchFieldException ignored) {
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = EventPublisher.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
