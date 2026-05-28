package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.AdminTagListResponseDTO;
import cn.idealer01.api.dto.AdminTagMemberListResponseDTO;
import cn.idealer01.api.dto.AdminTagMemberRequestDTO;
import cn.idealer01.api.dto.AdminTagUpsertRequestDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.ICrowdTagsDao;
import cn.idealer01.infrastructure.dao.ICrowdTagsDetailDao;
import cn.idealer01.infrastructure.dao.IMarketUserDao;
import cn.idealer01.infrastructure.dao.po.CrowdTags;
import cn.idealer01.infrastructure.dao.po.CrowdTagsDetail;
import cn.idealer01.infrastructure.dao.po.MarketUser;
import cn.idealer01.infrastructure.redis.IRedisService;
import cn.idealer01.types.enums.ResponseCode;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBitSet;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/admin/tags")
public class AdminTagController {

    private final ICrowdTagsDao tagsDao;
    private final ICrowdTagsDetailDao detailDao;
    private final IMarketUserDao userDao;
    private final IRedisService redisService;

    public AdminTagController(ICrowdTagsDao tagsDao, ICrowdTagsDetailDao detailDao, IMarketUserDao userDao, IRedisService redisService) {
        this.tagsDao = tagsDao;
        this.detailDao = detailDao;
        this.userDao = userDao;
        this.redisService = redisService;
    }

    @GetMapping
    public Response<AdminTagListResponseDTO> queryTags() {
        List<AdminTagListResponseDTO.TagItem> tagList = tagsDao.queryCrowdTagsList().stream().map(item -> AdminTagListResponseDTO.TagItem.builder()
                .tagId(item.getTagId())
                .tagName(item.getTagName())
                .tagDesc(item.getTagDesc())
                .statistics(item.getStatistics())
                .build()).collect(Collectors.toList());
        return Response.<AdminTagListResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(AdminTagListResponseDTO.builder().tagList(tagList).build())
                .build();
    }

    @PostMapping
    public Response<Void> createTag(@RequestBody AdminTagUpsertRequestDTO request) {
        if (request == null || StringUtils.isBlank(request.getTagId()) || StringUtils.isBlank(request.getTagName())) {
            return illegal();
        }
        tagsDao.insertCrowdTags(CrowdTags.builder()
                .tagId(request.getTagId())
                .tagName(request.getTagName())
                .tagDesc(request.getTagDesc())
                .statistics(0)
                .build());
        return success();
    }

    @PutMapping("/{tagId}")
    public Response<Void> updateTag(@PathVariable String tagId, @RequestBody AdminTagUpsertRequestDTO request) {
        if (request == null || StringUtils.isBlank(tagId) || StringUtils.isBlank(request.getTagName())) {
            return illegal();
        }
        tagsDao.updateCrowdTags(CrowdTags.builder()
                .tagId(tagId)
                .tagName(request.getTagName())
                .tagDesc(request.getTagDesc())
                .build());
        return success();
    }

    @GetMapping("/{tagId}/members")
    public Response<AdminTagMemberListResponseDTO> queryMembers(@PathVariable String tagId) {
        List<CrowdTagsDetail> detailList = detailDao.queryCrowdTagsDetailListByTagId(tagId);
        Map<String, MarketUser> usersById = detailList.stream()
                .map(CrowdTagsDetail::getUserId)
                .distinct()
                .map(userDao::queryMarketUserByUserId)
                .filter(user -> user != null)
                .collect(Collectors.toMap(MarketUser::getUserId, user -> user));

        List<AdminTagMemberListResponseDTO.MemberItem> members = detailList.stream().map(detail -> {
            MarketUser user = usersById.get(detail.getUserId());
            return AdminTagMemberListResponseDTO.MemberItem.builder()
                    .userId(detail.getUserId())
                    .displayName(user == null ? detail.getUserId() : user.getDisplayName())
                    .loginType(user == null ? "FINGERPRINT" : user.getLoginType())
                    .status(user == null ? 0 : user.getStatus())
                    .build();
        }).collect(Collectors.toList());

        return Response.<AdminTagMemberListResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(AdminTagMemberListResponseDTO.builder().memberList(members).build())
                .build();
    }

    @PostMapping("/{tagId}/members")
    public Response<Void> addMember(@PathVariable String tagId, @RequestBody AdminTagMemberRequestDTO request) {
        if (request == null || StringUtils.isBlank(tagId) || StringUtils.isBlank(request.getUserId())) {
            return illegal();
        }
        if (tagsDao.queryCrowdTagsByTagId(tagId) == null || userDao.queryMarketUserByUserId(request.getUserId()) == null) {
            return illegal();
        }
        try {
            detailDao.addCrowdTagsUserId(CrowdTagsDetail.builder().tagId(tagId).userId(request.getUserId()).build());
        } catch (DuplicateKeyException ignore) {
        }
        syncTagBit(tagId, request.getUserId(), true);
        refreshStatistics(tagId);
        return success();
    }

    @DeleteMapping("/{tagId}/members/{userId}")
    public Response<Void> removeMember(@PathVariable String tagId, @PathVariable String userId) {
        detailDao.deleteCrowdTagsUserId(tagId, userId);
        syncTagBit(tagId, userId, false);
        refreshStatistics(tagId);
        return success();
    }

    private void syncTagBit(String tagId, String userId, boolean value) {
        RBitSet bitSet = redisService.getBitSet(tagId);
        bitSet.set(redisService.getIndexFromUserId(userId), value);
    }

    private void refreshStatistics(String tagId) {
        tagsDao.updateCrowdTagsStatisticsTo(tagId, detailDao.countCrowdTagsDetailByTagId(tagId));
    }

    private Response<Void> success() {
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    private Response<Void> illegal() {
        return Response.<Void>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info(ResponseCode.ILLEGAL_PARAMETER.getInfo()).build();
    }
}
