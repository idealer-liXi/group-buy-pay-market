package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.CancelOrderRequestDTO;
import cn.idealer01.api.dto.MockPayRequestDTO;
import cn.idealer01.api.dto.PurchaseHistoryResponseDTO;
import cn.idealer01.api.dto.RefundPaidOrderRequestDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.order.model.valobj.OrderStatusVO;
import cn.idealer01.domain.order.service.IOrderService;
import cn.idealer01.infrastructure.dao.IOrderDao;
import cn.idealer01.infrastructure.dao.po.PayOrder;
import cn.idealer01.trigger.websocket.UserNotificationWebSocketHandler;
import cn.idealer01.types.enums.ResponseCode;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/order/")
public class PurchaseHistoryController {

    @Resource
    private IOrderDao orderDao;
    @Resource
    private IOrderService orderService;
    @Resource
    private PurchaseStatusResolver purchaseStatusResolver;
    @Resource
    private AlipayClient alipayClient;
    @Resource
    private UserNotificationWebSocketHandler userNotificationWebSocketHandler;
    @Value("${alipay.refund-enabled:false}")
    private boolean alipayRefundEnabled;

    public PurchaseHistoryController() {
    }

    public PurchaseHistoryController(IOrderDao orderDao) {
        this.orderDao = orderDao;
        this.purchaseStatusResolver = new PurchaseStatusResolver();
    }

    public PurchaseHistoryController(IOrderDao orderDao, AlipayClient alipayClient) {
        this.orderDao = orderDao;
        this.alipayClient = alipayClient;
        this.purchaseStatusResolver = new PurchaseStatusResolver();
    }

    public PurchaseHistoryController(IOrderDao orderDao, AlipayClient alipayClient, UserNotificationWebSocketHandler userNotificationWebSocketHandler) {
        this.orderDao = orderDao;
        this.alipayClient = alipayClient;
        this.userNotificationWebSocketHandler = userNotificationWebSocketHandler;
        this.purchaseStatusResolver = new PurchaseStatusResolver();
    }

    public PurchaseHistoryController(IOrderDao orderDao, PurchaseStatusResolver purchaseStatusResolver) {
        this.orderDao = orderDao;
        this.purchaseStatusResolver = purchaseStatusResolver;
    }

    @GetMapping("query_purchase_history")
    public Response<PurchaseHistoryResponseDTO> queryPurchaseHistory(@RequestParam String userId) {
        try {
            if (StringUtils.isBlank(userId)) {
                return Response.<PurchaseHistoryResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            List<PayOrder> payOrderList = orderDao.queryPayOrderListByUserId(userId);
            List<PurchaseHistoryResponseDTO.Record> recordList = new ArrayList<>();
            if (null != payOrderList) {
                for (PayOrder payOrder : payOrderList) {
                    String statusType = purchaseStatusResolver.resolve(payOrder);
                    if (StringUtils.isBlank(statusType)) {
                        continue;
                    }
                    recordList.add(PurchaseHistoryResponseDTO.Record.builder()
                            .orderId(payOrder.getOrderId())
                            .outTradeNo(payOrder.getOrderId())
                            .productId(payOrder.getProductId())
                            .productName(payOrder.getProductName())
                            .orderTime(payOrder.getOrderTime())
                            .totalAmount(payOrder.getTotalAmount())
                            .payAmount(payOrder.getPayAmount())
                            .payUrl(payOrder.getPayUrl())
                            .status(payOrder.getStatus())
                            .statusType(statusType)
                            .marketType(payOrder.getMarketType())
                            .purchaseType(toPurchaseType(payOrder.getMarketType()))
                            .build());
                }
            }

            return Response.<PurchaseHistoryResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(PurchaseHistoryResponseDTO.builder().recordList(recordList).build())
                    .build();
        } catch (Exception e) {
            log.error("查询购物记录失败 userId:{}", userId, e);
            return Response.<PurchaseHistoryResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("cancel_order")
    public Response<Boolean> cancelOrder(@RequestBody CancelOrderRequestDTO requestDTO) {
        try {
            if (null == requestDTO || StringUtils.isBlank(requestDTO.getUserId()) || StringUtils.isBlank(requestDTO.getOrderId())) {
                return Response.<Boolean>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            PayOrder payOrder = orderDao.queryOrderByOrderId(requestDTO.getOrderId());
            if (null == payOrder || !requestDTO.getUserId().equals(payOrder.getUserId()) || (null != payOrder.getMarketType() && payOrder.getMarketType() == 1)) {
                return Response.<Boolean>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            boolean status = orderDao.changeOrderClose(requestDTO.getOrderId());
            if (status) {
                pushRefundSuccess(requestDTO.getUserId(), requestDTO.getOrderId());
            }
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(status)
                    .build();
        } catch (Exception e) {
            log.error("取消普通未支付订单失败 userId:{} orderId:{}", null == requestDTO ? null : requestDTO.getUserId(), null == requestDTO ? null : requestDTO.getOrderId(), e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("refund_paid_order")
    public Response<Boolean> refundPaidOrder(@RequestBody RefundPaidOrderRequestDTO requestDTO) {
        try {
            if (null == requestDTO || StringUtils.isBlank(requestDTO.getUserId()) || StringUtils.isBlank(requestDTO.getOrderId())) {
                return Response.<Boolean>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            PayOrder payOrder = orderDao.queryPayOrderByOrderId(requestDTO.getOrderId());
            if (!canRefundPaidPlainOrder(requestDTO, payOrder)) {
                return Response.<Boolean>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            if (alipayRefundEnabled) {
                AlipayTradeRefundResponse refundResponse = refundPaidPlainOrder(payOrder);
                if (!isAlipayRefundSuccess(refundResponse)) {
                    return Response.<Boolean>builder()
                            .code(ResponseCode.UN_ERROR.getCode())
                            .info(StringUtils.defaultIfBlank(null == refundResponse ? null : refundResponse.getSubMsg(), ResponseCode.UN_ERROR.getInfo()))
                            .data(false)
                            .build();
                }
            }

            boolean status = orderDao.changeOrderClose(requestDTO.getOrderId());
            if (status) {
                pushRefundSuccess(requestDTO.getUserId(), requestDTO.getOrderId());
            }
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(status)
                    .build();
        } catch (Exception e) {
            log.error("普通已支付订单退款失败 userId:{} orderId:{}", null == requestDTO ? null : requestDTO.getUserId(), null == requestDTO ? null : requestDTO.getOrderId(), e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }

    @PostMapping("mock_pay")
    public Response<Boolean> mockPay(@RequestBody MockPayRequestDTO requestDTO) {
        try {
            if (null == requestDTO || StringUtils.isBlank(requestDTO.getUserId()) || StringUtils.isBlank(requestDTO.getOrderId()) || StringUtils.isBlank(requestDTO.getPassword())) {
                return Response.<Boolean>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .data(false)
                        .build();
            }

            if (!"111111".equals(requestDTO.getPassword())) {
                return Response.<Boolean>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info("支付密码错误")
                        .data(false)
                        .build();
            }

            PayOrder payOrder = orderDao.queryOrderByOrderId(requestDTO.getOrderId());
            if (null == payOrder || !requestDTO.getUserId().equals(payOrder.getUserId()) || !OrderStatusVO.PAY_WAIT.getCode().equals(payOrder.getStatus())) {
                return Response.<Boolean>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .data(false)
                        .build();
            }

            orderService.changeOrderPaySuccess(requestDTO.getOrderId(), new java.util.Date());
            if (null != payOrder.getMarketType() && payOrder.getMarketType() == 1) {
                pushPaySuccess(payOrder.getUserId(), payOrder.getOrderId());
            }
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (Exception e) {
            log.error("模拟支付失败 userId:{} orderId:{}", null == requestDTO ? null : requestDTO.getUserId(), null == requestDTO ? null : requestDTO.getOrderId(), e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }

    private boolean canRefundPaidPlainOrder(RefundPaidOrderRequestDTO requestDTO, PayOrder payOrder) {
        if (null == payOrder || !requestDTO.getUserId().equals(payOrder.getUserId())) {
            return false;
        }
        if (null != payOrder.getMarketType() && payOrder.getMarketType() == 1) {
            return false;
        }
        if (!"PAY_SUCCESS".equals(payOrder.getStatus()) && !"DEAL_DONE".equals(payOrder.getStatus())) {
            return false;
        }
        return null != payOrder.getPayAmount() && payOrder.getPayAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    private AlipayTradeRefundResponse refundPaidPlainOrder(PayOrder payOrder) throws Exception {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payOrder.getOrderId());
        bizContent.put("refund_amount", payOrder.getPayAmount().toPlainString());
        bizContent.put("refund_reason", "用户申请退款");
        bizContent.put("out_request_no", payOrder.getOrderId() + "_REFUND");
        request.setBizContent(bizContent.toString());
        return alipayClient.execute(request);
    }

    private boolean isAlipayRefundSuccess(AlipayTradeRefundResponse refundResponse) {
        return null != refundResponse && refundResponse.isSuccess() && (StringUtils.isBlank(refundResponse.getCode()) || "10000".equals(refundResponse.getCode()));
    }

    private void pushRefundSuccess(String userId, String orderId) {
        if (userNotificationWebSocketHandler == null || StringUtils.isBlank(userId)) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "REFUND_SUCCESS");
        payload.put("orderId", orderId);
        payload.put("message", "退单成功");
        userNotificationWebSocketHandler.sendToUsers(Collections.singleton(userId), com.alibaba.fastjson.JSON.toJSONString(payload));
    }

    private void pushPaySuccess(String userId, String orderId) {
        if (userNotificationWebSocketHandler == null || StringUtils.isBlank(userId)) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "PAY_SUCCESS");
        payload.put("orderId", orderId);
        payload.put("message", "支付成功");
        userNotificationWebSocketHandler.sendToUsers(Collections.singleton(userId), com.alibaba.fastjson.JSON.toJSONString(payload));
    }

    private String toPurchaseType(Integer marketType) {
        return null != marketType && marketType == 1 ? "GROUP_BUY" : "PLAIN";
    }

}
