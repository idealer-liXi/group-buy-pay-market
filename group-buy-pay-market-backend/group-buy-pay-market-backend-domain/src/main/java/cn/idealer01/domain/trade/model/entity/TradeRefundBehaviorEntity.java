package cn.idealer01.domain.trade.model.entity;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TradeRefundBehaviorEntity {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 组队ID
     */
    private String teamId;

    private TradeRefundBehaviorEnum tradeRefundBehaviorEnum;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public enum TradeRefundBehaviorEnum{

        SUCCESS("success", "成功"),
        REPEAT("repeat", "重复"),
        FAIL("fail", "失败");

        private String code;
        private String info;

    }

}
