package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseHistoryResponseDTO {

    private List<Record> recordList;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Record {
        private String orderId;
        private String outTradeNo;
        private String productId;
        private String productName;
        private Date orderTime;
        private BigDecimal totalAmount;
        private BigDecimal payAmount;
        private String payUrl;
        private String status;
        private String statusType;
        private Integer marketType;
        private String purchaseType;
    }
}
