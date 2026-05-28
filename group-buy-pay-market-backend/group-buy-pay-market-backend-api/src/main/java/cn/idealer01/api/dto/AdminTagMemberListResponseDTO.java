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
public class AdminTagMemberListResponseDTO {
    private List<MemberItem> memberList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberItem {
        private String userId;
        private String displayName;
        private String loginType;
        private Integer status;
    }
}
