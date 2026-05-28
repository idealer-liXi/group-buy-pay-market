package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDiscountListResponseDTO {
    private List<DiscountItem> discountList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountItem {
        private String discountId;
        private String discountName;
        private String discountDesc;
        private Integer discountType;
        private String marketPlan;
        private String marketExpr;
        private String tagId;
        private Integer status;
    }
}
