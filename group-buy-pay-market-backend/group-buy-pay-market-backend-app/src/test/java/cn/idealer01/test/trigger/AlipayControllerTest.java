package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.CreatePayRequestDTO;
import cn.idealer01.domain.order.model.entity.PayOrderEntity;
import cn.idealer01.domain.order.model.entity.ShopCartEntity;
import cn.idealer01.domain.order.model.valobj.MarketTypeVO;
import cn.idealer01.domain.order.service.IOrderService;
import cn.idealer01.trigger.http.AlipayController;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AlipayControllerTest {

    @Test
    public void createPayOrder_passesTeamIdToOrderServiceWhenJoiningExistingTeam() throws Exception {
        IOrderService orderService = mock(IOrderService.class);
        when(orderService.createOrder(any(ShopCartEntity.class))).thenReturn(PayOrderEntity.builder()
                .orderId("order-1")
                .payUrl("pay-form")
                .build());

        AlipayController controller = new AlipayController();
        ReflectionTestUtils.setField(controller, "orderService", orderService);

        CreatePayRequestDTO request = new CreatePayRequestDTO();
        request.setUserId("u1");
        request.setProductId("9890001");
        request.setActivityId(100123L);
        request.setTeamId("team-1");
        request.setMarketType(MarketTypeVO.GROUP_BUY_MARKET.getCode());

        controller.createPayOrder(request);

        ArgumentCaptor<ShopCartEntity> captor = ArgumentCaptor.forClass(ShopCartEntity.class);
        org.mockito.Mockito.verify(orderService).createOrder(captor.capture());
        assertEquals("team-1", captor.getValue().getTeamId());
    }
}
