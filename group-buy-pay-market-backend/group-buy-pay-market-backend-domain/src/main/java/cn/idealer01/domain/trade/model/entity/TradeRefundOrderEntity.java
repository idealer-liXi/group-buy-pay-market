package cn.idealer01.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeRefundOrderEntity {

    private String userId;
    private String teamId;
    private Long activityId;
    private String orderId;
    /**
     * 外部交易单号
     */
    private String outTradeNo;
}
