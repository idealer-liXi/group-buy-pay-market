package cn.idealer01.test.infrastructure;

import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

public class GroupBuyOrderInProgressSqlTest {

    @Test
    public void inProgressTeamQueries_filterByTeamValidEndTime() throws Exception {
        String orderListMapper = readResource("mybatis/mapper/IGroupBuyOrderListDao.xml");
        String orderMapper = readResource("mybatis/mapper/IGroupBuyOrderDao.xml");

        assertTrue(orderListMapper.contains("valid_end_time > now()"));
        assertTrue(orderMapper.contains("valid_end_time > now()"));
    }

    @Test
    public void completeOrderUpdate_isIdempotentForDuplicatePaymentCallbacks() throws Exception {
        String orderListMapper = readResource("mybatis/mapper/IGroupBuyOrderListDao.xml");

        assertTrue(orderListMapper.contains("where out_trade_no = #{outTradeNo} and user_id = #{userId} and status = 0"));
    }

    @Test
    public void purchaseHistoryQuery_loadsGroupOrderStateForEffectiveStatus() throws Exception {
        String payOrderMapper = readResource("mybatis/mapper/pay_order_mapper.xml");

        assertTrue(payOrderMapper.contains("group_buy_order_list"));
        assertTrue(payOrderMapper.contains("group_order_list_status"));
        assertTrue(payOrderMapper.contains("group_order_status"));
        assertTrue(payOrderMapper.contains("group_valid_end_time"));
    }

    @Test
    public void timeoutRefundQuery_loadsPaidExpiredUnformedGroupOrders() throws Exception {
        String orderListMapper = readResource("mybatis/mapper/IGroupBuyOrderListDao.xml");

        assertTrue(orderListMapper.contains("queryTimeoutPaidUnformedOrderList"));
        assertTrue(orderListMapper.contains("l.status = 1"));
        assertTrue(orderListMapper.contains("g.status = 0"));
        assertTrue(orderListMapper.contains("g.valid_end_time &lt;= now()"));
        assertTrue(orderListMapper.contains("g.complete_count &lt; g.target_count"));
    }

    private String readResource(String path) throws Exception {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (null == inputStream) {
                throw new IllegalArgumentException("Missing resource: " + path);
            }
            byte[] bytes = new byte[inputStream.available()];
            int read = inputStream.read(bytes);
            return new String(bytes, 0, read, StandardCharsets.UTF_8);
        }
    }
}
