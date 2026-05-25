package cn.idealer01.test.domain.trade;

import cn.idealer01.api.IMarketTradeService;
import cn.idealer01.api.dto.LockMarketPayOrderRequestDTO;
import cn.idealer01.api.dto.LockMarketPayOrderResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundBehaviorEntity;
import cn.idealer01.domain.trade.model.entity.TradeRefundCommandEntity;
import cn.idealer01.domain.trade.service.ITradeRefundOrderService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 锁单、恢复、锁单
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/7/29 10:34
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ITradeReverseStockServiceTest {

    @Resource
    private ITradeRefundOrderService tradeRefundOrderService;

    @Resource
    private IMarketTradeService marketTradeService;

    @Test
    public void test_refundOrder() throws Exception {
        TradeRefundCommandEntity tradeRefundCommandEntity = TradeRefundCommandEntity.builder()
                .userId("xfg803")
                .outTradeNo("657518652378")
                .source("s01")
                .channel("c01")
                .build();

        TradeRefundBehaviorEntity tradeRefundBehaviorEntity = tradeRefundOrderService.refundOrder(tradeRefundCommandEntity);

        log.info("请求参数:{}", JSON.toJSONString(tradeRefundCommandEntity));
        log.info("测试结果:{}", JSON.toJSONString(tradeRefundBehaviorEntity));

        // 暂停，等待MQ消息。处理完后，手动关闭程序
        new CountDownLatch(1).await();
    }

    @Test
    public void test_lockMarketPayOrder() throws InterruptedException {
        String teamId = null;
        for (int i = 1; i < 4; i++) {
            LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO = new LockMarketPayOrderRequestDTO();
            lockMarketPayOrderRequestDTO.setUserId("xfg80" + i);
            lockMarketPayOrderRequestDTO.setTeamId(teamId);
            lockMarketPayOrderRequestDTO.setActivityId(100123L);
            lockMarketPayOrderRequestDTO.setGoodsId("9890001");
            lockMarketPayOrderRequestDTO.setSource("s01");
            lockMarketPayOrderRequestDTO.setChannel("c01");
            lockMarketPayOrderRequestDTO.setNotifyMQ();
            lockMarketPayOrderRequestDTO.setOutTradeNo(RandomStringUtils.randomNumeric(12));

            Response<LockMarketPayOrderResponseDTO> lockMarketPayOrderResponseDTOResponse = marketTradeService.lockMarketPayOrder(lockMarketPayOrderRequestDTO);
            teamId = lockMarketPayOrderResponseDTOResponse.getData().getTeamId();

            log.info("第{}笔，测试结果 req:{} res:{}", i, JSON.toJSONString(lockMarketPayOrderRequestDTO), JSON.toJSONString(lockMarketPayOrderResponseDTOResponse));
        }

    }

    @Test
    public void test_lockMarketPayOrder_reverse() throws InterruptedException {
        LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO = new LockMarketPayOrderRequestDTO();
        lockMarketPayOrderRequestDTO.setUserId("xfg404");
        lockMarketPayOrderRequestDTO.setTeamId("06972139");
        lockMarketPayOrderRequestDTO.setActivityId(100123L);
        lockMarketPayOrderRequestDTO.setGoodsId("9890001");
        lockMarketPayOrderRequestDTO.setSource("s01");
        lockMarketPayOrderRequestDTO.setChannel("c01");
        lockMarketPayOrderRequestDTO.setNotifyMQ();
        lockMarketPayOrderRequestDTO.setOutTradeNo(RandomStringUtils.randomNumeric(12));

        Response<LockMarketPayOrderResponseDTO> lockMarketPayOrderResponseDTOResponse = marketTradeService.lockMarketPayOrder(lockMarketPayOrderRequestDTO);

        log.info("测试结果 req:{} res:{}", JSON.toJSONString(lockMarketPayOrderRequestDTO), JSON.toJSONString(lockMarketPayOrderResponseDTOResponse));
    }

    @Test
    public void test_queryTimeoutUnpaidOrderList2Refund() throws Exception {
        List<UserGroupBuyOrderDetailEntity> timeoutOrderList = tradeRefundOrderService.queryTimeoutUnpaidOrderList();

        log.info("查询超时未支付订单列表，数量：{}", timeoutOrderList != null ? timeoutOrderList.size() : 0);

        if (timeoutOrderList != null && !timeoutOrderList.isEmpty()) {
            for (UserGroupBuyOrderDetailEntity orderDetail : timeoutOrderList) {
                log.info("超时订单详情：用户ID={}, 团队ID={}, 活动ID={}, 外部交易单号={}, 有效开始时间={}, 有效结束时间={}",
                        orderDetail.getUserId(),
                        orderDetail.getTeamId(),
                        orderDetail.getActivityId(),
                        orderDetail.getOutTradeNo(),
                        orderDetail.getValidStartTime(),
                        orderDetail.getValidEndTime());

                TradeRefundCommandEntity tradeRefundCommandEntity = TradeRefundCommandEntity.builder()
                        .userId(orderDetail.getUserId())
                        .outTradeNo(orderDetail.getOutTradeNo())
                        .source(orderDetail.getSource())
                        .channel(orderDetail.getChannel())
                        .build();

                TradeRefundBehaviorEntity tradeRefundBehaviorEntity = tradeRefundOrderService.refundOrder(tradeRefundCommandEntity);

                log.info("请求参数(job):{}", JSON.toJSONString(tradeRefundCommandEntity));
                log.info("测试结果(job):{}", JSON.toJSONString(tradeRefundBehaviorEntity));
            }
        } else {
            log.info("当前没有超时未支付订单");
        }

        // 暂停，等待MQ消息。处理完后，手动关闭程序
        new CountDownLatch(1).await();
    }

}
