package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.AdminActivityListResponseDTO;
import cn.idealer01.api.dto.AdminActivityUpsertRequestDTO;
import cn.idealer01.api.dto.AdminStatusUpdateRequestDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.IGroupBuyActivityDao;
import cn.idealer01.infrastructure.dao.IGroupBuyDiscountDao;
import cn.idealer01.infrastructure.dao.ISCSkuActivityDao;
import cn.idealer01.infrastructure.dao.ISkuDao;
import cn.idealer01.infrastructure.dao.po.GroupBuyActivity;
import cn.idealer01.infrastructure.dao.po.GroupBuyDiscount;
import cn.idealer01.infrastructure.dao.po.SCSkuActivity;
import cn.idealer01.types.enums.ResponseCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/admin/activities")
public class AdminActivityController {

    private final IGroupBuyActivityDao activityDao;
    private final ISkuDao skuDao;
    private final IGroupBuyDiscountDao discountDao;
    private final ISCSkuActivityDao scSkuActivityDao;

    public AdminActivityController(IGroupBuyActivityDao activityDao, ISkuDao skuDao, IGroupBuyDiscountDao discountDao, ISCSkuActivityDao scSkuActivityDao) {
        this.activityDao = activityDao;
        this.skuDao = skuDao;
        this.discountDao = discountDao;
        this.scSkuActivityDao = scSkuActivityDao;
    }

    @GetMapping
    public Response<AdminActivityListResponseDTO> queryActivityList() {
        return Response.<AdminActivityListResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(AdminActivityListResponseDTO.builder()
                        .activityList(activityDao.queryGroupBuyActivityList().stream().map(item -> {
                            List<SCSkuActivity> bindings = scSkuActivityDao.querySCSkuActivityListByActivityId(item.getActivityId());
                            String goodsIds = bindings == null ? null : bindings.stream()
                                    .map(SCSkuActivity::getGoodsId)
                                    .collect(Collectors.joining(","));
                            return AdminActivityListResponseDTO.ActivityItem.builder()
                                    .activityId(item.getActivityId())
                                    .activityName(item.getActivityName())
                                    .goodsId(goodsIds)
                                    .discountId(item.getDiscountId())
                                    .groupType(item.getGroupType())
                                    .takeLimitCount(item.getTakeLimitCount())
                                    .target(item.getTarget())
                                    .validTime(item.getValidTime())
                                    .status(item.getStatus())
                                    .startTime(item.getStartTime())
                                    .endTime(item.getEndTime())
                                    .tagId(item.getTagId())
                                    .tagScope(item.getTagScope())
                                    .build();
                        }).collect(Collectors.toList()))
                        .build())
                .build();
    }

    @PostMapping
    public Response<Void> createActivity(@RequestBody AdminActivityUpsertRequestDTO request) {
        if (request == null || request.getStartTime() == null || request.getEndTime() == null
                || request.getStartTime().compareTo(request.getEndTime()) >= 0
                || request.getTarget() == null || request.getTarget() < 2
                || request.getValidTime() == null || request.getValidTime() <= 0) {
            return Response.<Void>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info(ResponseCode.ILLEGAL_PARAMETER.getInfo()).build();
        }

        List<String> goodsIds = parseGoodsIds(request.getGoodsId());

        GroupBuyDiscount discount = discountDao.queryGroupBuyActivityDiscountByDiscountId(request.getDiscountId());
        if (discount == null || (discount.getStatus() != null && discount.getStatus() == 1)) {
            return Response.<Void>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("折扣不存在或已停用").build();
        }

        Response<Void> goodsValidation = validateGoodsIds(goodsIds, null);
        if (goodsValidation != null) {
            return goodsValidation;
        }

        Long activityId = request.getActivityId() == null
                ? activityDao.queryGroupBuyActivityList().stream()
                .map(GroupBuyActivity::getActivityId)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(100000L) + 1
                : request.getActivityId();

        activityDao.insertGroupBuyActivity(GroupBuyActivity.builder()
                .activityId(activityId)
                .activityName(request.getActivityName())
                .discountId(request.getDiscountId())
                .groupType(request.getGroupType())
                .takeLimitCount(request.getTakeLimitCount())
                .target(request.getTarget())
                .validTime(request.getValidTime())
                .status(0)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .tagId(request.getTagId())
                .tagScope(request.getTagScope())
                .build());

        insertGoodsBindings(activityId, goodsIds);

        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    @PutMapping("/{activityId}")
    public Response<Void> updateActivity(@PathVariable Long activityId, @RequestBody AdminActivityUpsertRequestDTO request) {
        GroupBuyActivity current = activityDao.queryGroupBuyActivityByActivityId(activityId);
        if (current != null && current.getStatus() != null && current.getStatus() == 1) {
            return Response.<Void>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("已生效活动不可修改绑定关系").build();
        }

        if (request == null || request.getStartTime() == null || request.getEndTime() == null
                || request.getStartTime().compareTo(request.getEndTime()) >= 0
                || request.getTarget() == null || request.getTarget() < 2
                || request.getValidTime() == null || request.getValidTime() <= 0) {
            return Response.<Void>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info(ResponseCode.ILLEGAL_PARAMETER.getInfo()).build();
        }

        List<String> goodsIds = parseGoodsIds(request.getGoodsId());

        GroupBuyDiscount discount = discountDao.queryGroupBuyActivityDiscountByDiscountId(request.getDiscountId());
        if (discount == null || (discount.getStatus() != null && discount.getStatus() == 1)) {
            return Response.<Void>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("折扣不存在或已停用").build();
        }

        Response<Void> goodsValidation = validateGoodsIds(goodsIds, activityId);
        if (goodsValidation != null) {
            return goodsValidation;
        }

        activityDao.updateGroupBuyActivity(GroupBuyActivity.builder()
                .activityId(activityId)
                .activityName(request.getActivityName())
                .discountId(request.getDiscountId())
                .groupType(request.getGroupType())
                .takeLimitCount(request.getTakeLimitCount())
                .target(request.getTarget())
                .validTime(request.getValidTime())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .tagId(request.getTagId())
                .tagScope(request.getTagScope())
                .build());

        if (goodsIds.size() == 1) {
            scSkuActivityDao.updateSCSkuActivityByActivityId(activityId, goodsIds.get(0), "s01", "c01");
        } else {
            scSkuActivityDao.deleteSCSkuActivityByActivityId(activityId);
            insertGoodsBindings(activityId, goodsIds);
        }

        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    @PutMapping("/{activityId}/status")
    public Response<Void> updateStatus(@PathVariable Long activityId, @RequestBody AdminStatusUpdateRequestDTO request) {
        activityDao.updateGroupBuyActivityStatus(activityId, request.getStatus());
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    private List<String> parseGoodsIds(String goodsId) {
        return Arrays.stream(StringUtils.split(StringUtils.defaultString(goodsId), ','))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private Response<Void> validateGoodsIds(List<String> goodsIds, Long currentActivityId) {
        if (goodsIds.isEmpty()) {
            return Response.<Void>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("商品不存在或已停用").build();
        }

        for (String goodsId : goodsIds) {
            if (skuDao.querySkuByGoodsId(goodsId) == null || (skuDao.querySkuByGoodsId(goodsId).getStatus() != null && skuDao.querySkuByGoodsId(goodsId).getStatus() == 1)) {
                return Response.<Void>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("商品不存在或已停用").build();
            }

            SCSkuActivity existingBinding = scSkuActivityDao.querySCSkuActivityBySCGoodsId(SCSkuActivity.builder()
                    .source("s01")
                    .channel("c01")
                    .goodsId(goodsId)
                    .build());
            if (existingBinding != null && (currentActivityId == null || !currentActivityId.equals(existingBinding.getActivityId()))) {
                return Response.<Void>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("商品已绑定其他活动").build();
            }
        }
        return null;
    }

    private void insertGoodsBindings(Long activityId, List<String> goodsIds) {
        for (String goodsId : goodsIds) {
            scSkuActivityDao.insertSCSkuActivity(SCSkuActivity.builder()
                    .source("s01")
                    .channel("c01")
                    .goodsId(goodsId)
                    .activityId(activityId)
                    .build());
        }
    }
}
