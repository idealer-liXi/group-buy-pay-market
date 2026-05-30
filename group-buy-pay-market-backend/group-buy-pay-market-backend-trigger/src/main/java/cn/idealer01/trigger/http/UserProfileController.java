package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.AdminTagListResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.ICrowdTagsDao;
import cn.idealer01.infrastructure.dao.ICrowdTagsDetailDao;
import cn.idealer01.types.enums.ResponseCode;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/user")
public class UserProfileController {

    private final ICrowdTagsDetailDao detailDao;
    private final ICrowdTagsDao tagsDao;

    public UserProfileController(ICrowdTagsDetailDao detailDao, ICrowdTagsDao tagsDao) {
        this.detailDao = detailDao;
        this.tagsDao = tagsDao;
    }

    @GetMapping("/tags")
    public Response<AdminTagListResponseDTO> queryUserTags(@RequestParam String userId) {
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
