package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SkuListResponseDTO {

    private List<SkuItem> skuList;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkuItem {
        private String goodsId;
        private String goodsName;
        private BigDecimal originalPrice;
        private String coverImageUrl;
    }
}
