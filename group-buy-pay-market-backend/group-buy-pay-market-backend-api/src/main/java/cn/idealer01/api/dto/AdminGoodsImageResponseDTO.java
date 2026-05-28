package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminGoodsImageResponseDTO {
    private Long imageId;
    private String imageUrl;
    private Integer sortOrder;
}
