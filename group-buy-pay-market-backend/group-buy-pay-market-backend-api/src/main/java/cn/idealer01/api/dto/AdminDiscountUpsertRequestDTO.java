package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDiscountUpsertRequestDTO {
    private String discountId;
    private String discountName;
    private String discountDesc;
    private Integer discountType;
    private String marketPlan;
    private String marketExpr;
    private String tagId;
}
