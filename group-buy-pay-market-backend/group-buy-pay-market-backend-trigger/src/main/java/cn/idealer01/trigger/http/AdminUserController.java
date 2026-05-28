package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.AdminTagListResponseDTO;
import cn.idealer01.api.dto.AdminUserListResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.ICrowdTagsDao;
import cn.idealer01.infrastructure.dao.ICrowdTagsDetailDao;
import cn.idealer01.infrastructure.dao.IMarketUserDao;
import cn.idealer01.types.enums.ResponseCode;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final IMarketUserDao userDao;
    private final ICrowdTagsDetailDao detailDao;
    private final ICrowdTagsDao tagsDao;

    public AdminUserController(IMarketUserDao userDao, ICrowdTagsDetailDao detailDao, ICrowdTagsDao tagsDao) {
        this.userDao = userDao;
        this.detailDao = detailDao;
        this.tagsDao = tagsDao;
    }

    @GetMapping
    public Response<AdminUserListResponseDTO> queryUsers(@RequestParam(required = false) String keyword) {
        List<AdminUserListResponseDTO.UserItem> users = userDao.queryMarketUserList(keyword).stream()
                .map(user -> AdminUserListResponseDTO.UserItem.builder()
                        .userId(user.getUserId())
                        .displayName(user.getDisplayName())
                        .loginType(user.getLoginType())
                        .status(user.getStatus())
                        .firstLoginTime(user.getFirstLoginTime())
                        .lastLoginTime(user.getLastLoginTime())
                        .build())
                .collect(Collectors.toList());

        return Response.<AdminUserListResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(AdminUserListResponseDTO.builder().userList(users).build())
                .build();
    }

    @GetMapping("/{userId}/tags")
    public Response<AdminTagListResponseDTO> queryUserTags(@PathVariable String userId) {
        List<AdminTagListResponseDTO.TagItem> tags = detailDao.queryCrowdTagsDetailListByUserId(userId).stream()
                .map(detail -> tagsDao.queryCrowdTagsByTagId(detail.getTagId()))
                .filter(tag -> tag != null)
                .map(tag -> AdminTagListResponseDTO.TagItem.builder()
                        .tagId(tag.getTagId())
                        .tagName(tag.getTagName())
                        .tagDesc(tag.getTagDesc())
                        .statistics(tag.getStatistics())
                        .build())
                .collect(Collectors.toList());

        return Response.<AdminTagListResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(AdminTagListResponseDTO.builder().tagList(tags).build())
                .build();
    }
}
