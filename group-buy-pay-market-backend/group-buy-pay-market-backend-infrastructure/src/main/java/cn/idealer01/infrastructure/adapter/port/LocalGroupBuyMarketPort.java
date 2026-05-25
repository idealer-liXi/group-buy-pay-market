package cn.idealer01.infrastructure.adapter.port;

import cn.idealer01.api.IMarketTradeService;
import cn.idealer01.api.dto.LockMarketPayOrderRequestDTO;
import cn.idealer01.api.dto.LockMarketPayOrderResponseDTO;
import cn.idealer01.api.dto.SettlementMarketPayOrderRequestDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.order.model.valobj.MarketPayDiscountEntity;
import cn.idealer01.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Component
public class LocalGroupBuyMarketPort {

    @Resource
    private IMarketTradeService marketTradeService;

    public MarketPayDiscountEntity lockMarketPayOrder(String userId, String teamId, Long activityId,
                                                      String productId, String orderId, String source,
                                                      String channel) {
        LockMarketPayOrderRequestDTO requestDTO = new LockMarketPayOrderRequestDTO();
        requestDTO.setUserId(userId);
        requestDTO.setTeamId(teamId);
        requestDTO.setActivityId(activityId);
        requestDTO.setGoodsId(productId);
        requestDTO.setSource(source);
        requestDTO.setChannel(channel);
        requestDTO.setNotifyMQ();
        requestDTO.setOutTradeNo(orderId);

        Response<LockMarketPayOrderResponseDTO> response = marketTradeService.lockMarketPayOrder(requestDTO);
        log.info("本地营销锁单{} requestDTO:{} responseDTO:{}", userId, JSON.toJSONString(requestDTO), JSON.toJSONString(response));

        if (null == response) {
            return null;
        }

        if (!"0000".equals(response.getCode())) {
            throw new AppException(response.getCode(), response.getInfo());
        }

        return toMarketPayDiscountEntity(response.getData());
    }

    public void settlementMarketPayOrder(String userId, String orderId, Date payTime, String source, String channel) {
        SettlementMarketPayOrderRequestDTO requestDTO = new SettlementMarketPayOrderRequestDTO();
        requestDTO.setUserId(userId);
        requestDTO.setSource(source);
        requestDTO.setChannel(channel);
        requestDTO.setOutTradeNo(orderId);
        requestDTO.setOutTradeTime(payTime);

        Response<?> response = marketTradeService.settlementMarketPayOrder(requestDTO);
        log.info("本地营销结算{} requestDTO:{} responseDTO:{}", userId, JSON.toJSONString(requestDTO), JSON.toJSONString(response));

        if (null == response) {
            return;
        }

        if (!"0000".equals(response.getCode())) {
            throw new AppException(response.getCode(), response.getInfo());
        }
    }

    public static MarketPayDiscountEntity toMarketPayDiscountEntity(LockMarketPayOrderResponseDTO responseDTO) {
        if (null == responseDTO) {
            return null;
        }

        return MarketPayDiscountEntity.builder()
                .originalPrice(responseDTO.getOriginalPrice())
                .deductionPrice(responseDTO.getDeductionPrice())
                .payPrice(responseDTO.getPayPrice())
                .build();
    }
}
