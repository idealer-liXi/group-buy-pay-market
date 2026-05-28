package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminActivityListResponseDTO {
    private List<ActivityItem> activityList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityItem {
        private Long activityId;
        private String activityName;
        private String goodsId;
        private String discountId;
        private Integer groupType;
        private Integer takeLimitCount;
        private Integer target;
        private Integer validTime;
        private Integer status;
        private Date startTime;
        private Date endTime;
        private String tagId;
        private String tagScope;
    }
}
