package cn.idealer01.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TradeLockRuleCommandEntity {
    private String userId;
    private Long activityId;
    private String teamId;
}
