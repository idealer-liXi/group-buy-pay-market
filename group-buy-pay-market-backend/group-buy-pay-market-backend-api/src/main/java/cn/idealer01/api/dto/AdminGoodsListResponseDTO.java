package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminGoodsListResponseDTO {
    private List<GoodsItem> goodsList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoodsItem {
        private String goodsId;
        private String goodsName;
        private BigDecimal originalPrice;
        private Integer status;
    }
}
