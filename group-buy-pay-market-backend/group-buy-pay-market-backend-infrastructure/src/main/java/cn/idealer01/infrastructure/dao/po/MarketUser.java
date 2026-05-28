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
public class MarketUser {
    private Long id;
    private String userId;
    private String loginType;
    private String displayName;
    private Integer status;
    private Date firstLoginTime;
    private Date lastLoginTime;
    private Date createTime;
    private Date updateTime;
}
