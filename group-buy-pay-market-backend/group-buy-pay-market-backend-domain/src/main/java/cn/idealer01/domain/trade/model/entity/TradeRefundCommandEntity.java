package cn.idealer01.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TradeRefundCommandEntity {

    private String userId;

    private String outTradeNo;

    private String source;

    private String channel;
}
