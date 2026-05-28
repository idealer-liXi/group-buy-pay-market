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
public class AdminUserListResponseDTO {
    private List<UserItem> userList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserItem {
        private String userId;
        private String displayName;
        private String loginType;
        private Integer status;
        private Date firstLoginTime;
        private Date lastLoginTime;
    }
}
