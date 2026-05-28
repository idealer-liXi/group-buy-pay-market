package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminActivityUpsertRequestDTO {
    private Long activityId;
    private String activityName;
    private String goodsId;
    private String discountId;
    private Integer groupType;
    private Integer takeLimitCount;
    private Integer target;
    private Integer validTime;
    private Date startTime;
    private Date endTime;
    private String tagId;
    private String tagScope;
}
