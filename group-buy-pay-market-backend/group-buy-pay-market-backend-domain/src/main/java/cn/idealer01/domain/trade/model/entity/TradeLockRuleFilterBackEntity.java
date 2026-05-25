package cn.idealer01.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TradeLockRuleFilterBackEntity {
    // 用户参与活动的订单量
    private Integer userTakeOrderCount;
    //
    private String recoveryTeamStockKey;
}
