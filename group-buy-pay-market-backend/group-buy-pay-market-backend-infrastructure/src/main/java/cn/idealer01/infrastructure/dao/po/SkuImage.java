package cn.idealer01.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuImage {
    private Long id;
    private String goodsId;
    private String imageUrl;
    private String ossObjectKey;
    private Integer sortOrder;
    private Date createTime;
    private Date updateTime;
}
