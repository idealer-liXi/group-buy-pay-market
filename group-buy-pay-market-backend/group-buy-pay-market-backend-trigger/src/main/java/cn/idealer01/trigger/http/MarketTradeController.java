package cn.idealer01.trigger.http;

import cn.idealer01.api.IMarketTradeService;
import cn.idealer01.api.dto.*;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.activity.model.entity.MarketProductEntity;
import cn.idealer01.domain.activity.model.entity.TrialBalanceEntity;
import cn.idealer01.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.idealer01.domain.activity.service.IIndexGroupBuyMarketService;
import cn.idealer01.domain.trade.model.entity.*;
import cn.idealer01.domain.trade.model.valobj.GroupBuyProgressVO;
import cn.idealer01.domain.trade.model.valobj.NotifyConfigVO;
import cn.idealer01.domain.trade.model.valobj.NotifyTypeEnumVO;
import cn.idealer01.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import cn.idealer01.domain.trade.service.ITradeLockOrderService;
import cn.idealer01.domain.trade.service.ITradeRefundOrderService;
import cn.idealer01.domain.trade.service.ITradeSettlementOrderService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/trade/")
public class MarketTradeController implements IMarketTradeService {
    @Resource
    private ITradeLockOrderService tradeOrderService;
    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;
    @Resource
    private ITradeSettlementOrderService tradeSettlementOrderService;

    @Resource
    private ITradeRefundOrderService tradeRefundOrderService;

    @Override
    @PostMapping("lock_market_pay_order")
    public Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(@RequestBody LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO) {
        try {
            String userId = lockMarketPayOrderRequestDTO.getUserId();
            String source = lockMarketPayOrderRequestDTO.getSource();
            String channel = lockMarketPayOrderRequestDTO.getChannel();
            String goodsId = lockMarketPayOrderRequestDTO.getGoodsId();
            Long activityId = lockMarketPayOrderRequestDTO.getActivityId();
            String outTradeNo = lockMarketPayOrderRequestDTO.getOutTradeNo();
            String teamId = lockMarketPayOrderRequestDTO.getTeamId();
            LockMarketPayOrderRequestDTO.NotifyConfigVO notifyConfigVO = lockMarketPayOrderRequestDTO.getNotifyConfigVO();

            log.info("营销交易锁单：{} LockMarketPayOrderRequestDTO:{}", userId, JSON.toJSONString(lockMarketPayOrderRequestDTO));

            if (StringUtils.isBlank(userId) || StringUtils.isBlank(source) || StringUtils.isBlank(channel) || StringUtils.isBlank(goodsId) || null == activityId || StringUtils.isBlank(outTradeNo)) {
                return Response.<LockMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            //查询outTradeNo 确定是否已经存在交易记录
            MarketPayOrderEntity marketPayOrderEntity = tradeOrderService.queryNoPayMarketPayOrderByOutTradeNo(userId, outTradeNo);
            if (null != marketPayOrderEntity && TradeOrderStatusEnumVO.CREATE.equals(marketPayOrderEntity.getTradeOrderStatusEnumVO())) {
                //已经锁单了
                LockMarketPayOrderResponseDTO lockMarketPayOrderResponseDTO = LockMarketPayOrderResponseDTO.builder()
                        .orderId(marketPayOrderEntity.getOrderId())
                        .originalPrice(marketPayOrderEntity.getOriginalPrice())
                        .deductionPrice(marketPayOrderEntity.getDeductionPrice())
                        .payPrice(marketPayOrderEntity.getPayPrice())
                        .tradeOrderStatus(marketPayOrderEntity.getTradeOrderStatusEnumVO().getCode())
                        .build();

                log.info("交易锁单已存在：{} marketPayOrderEntity:{}", userId, JSON.toJSONString(marketPayOrderEntity));
                return Response.<LockMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info(ResponseCode.SUCCESS.getInfo())
                        .data(lockMarketPayOrderResponseDTO)
                        .build();
            }

            //判断拼团进度是否完成
            if(null != teamId){
                GroupBuyProgressVO groupBuyProgressVO = tradeOrderService.queryGroupBuyProgress(teamId);
                if(null != groupBuyProgressVO && Objects.equals(groupBuyProgressVO.getTargetCount(),groupBuyProgressVO.getLockCount())){
                    log.info("交易锁单拦截-拼团目标完成：{} {}", userId, teamId);
                    return Response.<LockMarketPayOrderResponseDTO>builder()
                            .code(ResponseCode.E0006.getCode())
                            .info(ResponseCode.E0006.getInfo())
                            .build();
                }
            }

            //营销优惠试算
            TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(MarketProductEntity.builder()
                    .userId(userId)
                    .source(source)
                    .channel(channel)
                    .goodsId(goodsId)
                    .activityId(activityId)
                    .build());

            GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = trialBalanceEntity.getGroupBuyActivityDiscountVO();

            //拼团人群限定
            if(!trialBalanceEntity.getIsEnable() || !trialBalanceEntity.getIsVisible()){
                return Response.<LockMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.E0007.getCode())
                        .info(ResponseCode.E0007.getInfo())
                        .build();
            }

            //锁单
            marketPayOrderEntity = tradeOrderService.lockMarketPayOrder(
                    UserEntity.builder()
                            .userId(userId)
                            .build(),
                    PayActivityEntity.builder()
                            .teamId(teamId)
                            .activityId(activityId)
                            .activityName(groupBuyActivityDiscountVO.getActivityName())
                            .startTime(groupBuyActivityDiscountVO.getStartTime())
                            .endTime(groupBuyActivityDiscountVO.getEndTime())
                            .validTime(groupBuyActivityDiscountVO.getValidTime())
                            .targetCount(groupBuyActivityDiscountVO.getTarget())
                            .build(),
                    PayDiscountEntity.builder()
                            .source(source)
                            .channel(channel)
                            .goodsId(goodsId)
                            .goodsName(trialBalanceEntity.getGoodsName())
                            .originalPrice(trialBalanceEntity.getOriginalPrice())
                            .deductionPrice(trialBalanceEntity.getDeductionPrice())
                            .payPrice(trialBalanceEntity.getPayPrice())
                            .outTradeNo(outTradeNo)
                            .notifyConfigVO(NotifyConfigVO.builder()
                                    .notifyType(NotifyTypeEnumVO.valueOf(notifyConfigVO.getNotifyType()))
                                    .notifyMQ(notifyConfigVO.getNotifyMQ())
                                    .notifyUrl(notifyConfigVO.getNotifyUrl())
                                    .build())
                            .build()
            );

            log.info("新的交易锁单记录:{} marketPayOrderEntity:{}", userId, JSON.toJSONString(marketPayOrderEntity));

            //返回结果
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(LockMarketPayOrderResponseDTO.builder()
                            .orderId(marketPayOrderEntity.getOrderId())
                            .originalPrice(marketPayOrderEntity.getOriginalPrice())
                            .payPrice(marketPayOrderEntity.getPayPrice())
                            .deductionPrice(marketPayOrderEntity.getDeductionPrice())
                            .tradeOrderStatus(marketPayOrderEntity.getTradeOrderStatusEnumVO().getCode())
                            .teamId(marketPayOrderEntity.getTeamId())
                            .build())
                    .build();
        }catch (AppException e){
            log.error("营销交易锁单业务异常:{} lockMarketPayOrderRequestDTO:{}", lockMarketPayOrderRequestDTO.getUserId(), JSON.toJSONString(lockMarketPayOrderRequestDTO));
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("营销交易锁单服务失败:{} LockMarketPayOrderRequestDTO:{}", lockMarketPayOrderRequestDTO.getUserId(), JSON.toJSONString(lockMarketPayOrderRequestDTO), e);
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }

    }

    @Override
    @PostMapping("settlement_market_pay_order")
    public Response<SettlementMarketPayOrderResponseDTO> settlementMarketPayOrder(@RequestBody SettlementMarketPayOrderRequestDTO requestDTO) {
        try{
            log.info("营销交易组队结算开始:{} outTradeNo:{}", requestDTO.getUserId(), requestDTO.getOutTradeNo());

            //判空
            if(StringUtils.isBlank(requestDTO.getUserId()) || StringUtils.isBlank(requestDTO.getSource()) || StringUtils.isBlank(requestDTO.getChannel()) || StringUtils.isBlank(requestDTO.getOutTradeNo()) || null == requestDTO.getOutTradeTime()){
                return Response.<SettlementMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            //1.结算服务
            TradePaySettlementEntity tradePaySettlementEntity = tradeSettlementOrderService.settlementMarketPayOrder(TradePaySuccessEntity.builder()
                    .userId(requestDTO.getUserId())
                    .source(requestDTO.getSource())
                    .channel(requestDTO.getChannel())
                    .outTradeNo(requestDTO.getOutTradeNo())
                    .outTradeTime(requestDTO.getOutTradeTime())
                    .build());

            //2.封装返回结果
            SettlementMarketPayOrderResponseDTO responseDTO = SettlementMarketPayOrderResponseDTO.builder()
                    .userId(tradePaySettlementEntity.getUserId())
                    .activityId(tradePaySettlementEntity.getActivityId())
                    .teamId(tradePaySettlementEntity.getTeamId())
                    .outTradeNo(tradePaySettlementEntity.getOutTradeNo())
                    .build();

            log.info("营销交易组队结算完成:{} outTradeNo:{} response:{}", requestDTO.getUserId(), requestDTO.getOutTradeNo(), JSON.toJSONString(responseDTO));

            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(responseDTO)
                    .build();
        }catch (AppException e) {
            log.error("营销交易结算服务异常:{} LockMarketPayOrderRequestDTO:{}", requestDTO.getUserId(), JSON.toJSONString(requestDTO), e);
            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }catch (Exception e){
            log.error("营销交易结算服务失败：{} SettlementMarketPayOrderRequestDTO:{}", requestDTO.getUserId(), JSON.toJSONString(requestDTO));
            return  Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("refund_market_pay_order")
    @Override
    public Response<RefundMarketPayOrderResponseDTO> refundMarketPayOrder(@RequestBody RefundMarketPayOrderRequestDTO requestDTO) {
        try {
            log.info("营销拼团退单开始:{} outTradeNo:{}", requestDTO.getUserId(), requestDTO.getOutTradeNo());
            //1.检查参数
            if(StringUtils.isBlank(requestDTO.getUserId()) || StringUtils.isBlank(requestDTO.getOutTradeNo()) ||
                StringUtils.isBlank(requestDTO.getSource()) || StringUtils.isBlank(requestDTO.getChannel())){
                return Response.<RefundMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }
            //2.调用退单服务
            TradeRefundBehaviorEntity tradeRefundBehaviorEntity = tradeRefundOrderService.refundOrder(TradeRefundCommandEntity.builder()
                            .userId(requestDTO.getUserId())
                            .outTradeNo(requestDTO.getOutTradeNo())
                            .source(requestDTO.getSource())
                            .channel(requestDTO.getChannel())
                    .build());

            //3.封装结果返回
            RefundMarketPayOrderResponseDTO responseDTO = RefundMarketPayOrderResponseDTO.builder()
                    .userId(tradeRefundBehaviorEntity.getUserId())
                    .orderId(tradeRefundBehaviorEntity.getOrderId())
                    .teamId(tradeRefundBehaviorEntity.getTeamId())
                    .code(tradeRefundBehaviorEntity.getTradeRefundBehaviorEnum().getCode())
                    .info(tradeRefundBehaviorEntity.getTradeRefundBehaviorEnum().getInfo())
                    .build();


            log.info("营销拼团退单完成:{} outTradeNo:{} response:{}", requestDTO.getUserId(), requestDTO.getOutTradeNo(), JSON.toJSONString(responseDTO));

            return Response.<RefundMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(responseDTO)
                    .build();

        } catch (AppException e){
            log.error("营销拼团退单异常:{} RefundMarketPayOrderRequestDTO:{}", requestDTO.getUserId(), JSON.toJSONString(requestDTO), e);
            return Response.<RefundMarketPayOrderResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e){
            log.error("营销拼团退单失败:{} RefundMarketPayOrderRequestDTO:{}", requestDTO.getUserId(), JSON.toJSONString(requestDTO), e);
            return Response.<RefundMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
