package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.PurchaseHistoryResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.IOrderDao;
import cn.idealer01.infrastructure.dao.po.PayOrder;
import cn.idealer01.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/order/")
public class PurchaseHistoryController {

    @Resource
    private IOrderDao orderDao;
    @Resource
    private PurchaseStatusResolver purchaseStatusResolver;

    public PurchaseHistoryController() {
    }

    public PurchaseHistoryController(IOrderDao orderDao) {
        this.orderDao = orderDao;
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

    private String toPurchaseType(Integer marketType) {
        return null != marketType && marketType == 1 ? "GROUP_BUY" : "PLAIN";
    }

}
