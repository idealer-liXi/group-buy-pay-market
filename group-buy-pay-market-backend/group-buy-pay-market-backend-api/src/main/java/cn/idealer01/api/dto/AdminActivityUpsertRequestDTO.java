package cn.idealer01.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Shanghai")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Shanghai")
    private Date endTime;
    private String tagId;
    private String tagScope;
}
