package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.AdminDiscountListResponseDTO;
import cn.idealer01.api.dto.AdminDiscountUpsertRequestDTO;
import cn.idealer01.api.dto.AdminStatusUpdateRequestDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.IGroupBuyActivityDao;
import cn.idealer01.infrastructure.dao.IGroupBuyDiscountDao;
import cn.idealer01.infrastructure.dao.po.GroupBuyDiscount;
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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/admin/discounts")
public class AdminDiscountController {

    private static final Set<String> SUPPORTED_MARKET_PLANS = new HashSet<>(Arrays.asList("ZJ", "MJ", "N", "ZK"));

    private final IGroupBuyDiscountDao discountDao;
    @SuppressWarnings("unused")
    private final IGroupBuyActivityDao activityDao;

    public AdminDiscountController(IGroupBuyDiscountDao discountDao, IGroupBuyActivityDao activityDao) {
        this.discountDao = discountDao;
        this.activityDao = activityDao;
    }

    @GetMapping
    public Response<AdminDiscountListResponseDTO> queryDiscountList() {
        return Response.<AdminDiscountListResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(AdminDiscountListResponseDTO.builder()
                        .discountList(discountDao.queryGroupBuyDiscountList().stream().map(item ->
                                AdminDiscountListResponseDTO.DiscountItem.builder()
                                        .discountId(String.valueOf(item.getDiscountId()))
                                        .discountName(item.getDiscountName())
                                        .discountDesc(item.getDiscountDesc())
                                        .discountType(item.getDiscountType())
                                        .marketPlan(item.getMarketPlan())
                                        .marketExpr(item.getMarketExpr())
                                        .tagId(item.getTagId())
                                        .status(item.getStatus())
                                        .build()).collect(Collectors.toList()))
                        .build())
                .build();
    }

    @PostMapping
    public Response<Void> createDiscount(@RequestBody AdminDiscountUpsertRequestDTO request) {
        if (request == null || StringUtils.isBlank(request.getDiscountName()) || StringUtils.isBlank(request.getMarketPlan()) || StringUtils.isBlank(request.getMarketExpr())) {
            return Response.<Void>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info(ResponseCode.ILLEGAL_PARAMETER.getInfo()).build();
        }

        if (!SUPPORTED_MARKET_PLANS.contains(request.getMarketPlan())) {
            return Response.<Void>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info(ResponseCode.ILLEGAL_PARAMETER.getInfo()).build();
        }

        Integer discountId = StringUtils.isBlank(request.getDiscountId())
                ? discountDao.queryGroupBuyDiscountList().stream()
                .map(GroupBuyDiscount::getDiscountId)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1
                : Integer.valueOf(request.getDiscountId());

        discountDao.insertGroupBuyDiscount(GroupBuyDiscount.builder()
                .discountId(discountId)
                .discountName(request.getDiscountName())
                .discountDesc(request.getDiscountDesc())
                .discountType(request.getDiscountType())
                .marketPlan(request.getMarketPlan())
                .marketExpr(request.getMarketExpr())
                .tagId(request.getTagId())
                .status(0)
                .build());
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    @PutMapping("/{discountId}")
    public Response<Void> updateDiscount(@PathVariable String discountId, @RequestBody AdminDiscountUpsertRequestDTO request) {
        if (request == null || StringUtils.isBlank(request.getDiscountName()) || StringUtils.isBlank(request.getMarketPlan()) || StringUtils.isBlank(request.getMarketExpr()) || !SUPPORTED_MARKET_PLANS.contains(request.getMarketPlan())) {
            return Response.<Void>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info(ResponseCode.ILLEGAL_PARAMETER.getInfo()).build();
        }

        discountDao.updateGroupBuyDiscount(GroupBuyDiscount.builder()
                .discountId(Integer.valueOf(discountId))
                .discountName(request.getDiscountName())
                .discountDesc(request.getDiscountDesc())
                .discountType(request.getDiscountType())
                .marketPlan(request.getMarketPlan())
                .marketExpr(request.getMarketExpr())
                .tagId(request.getTagId())
                .build());
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    @PutMapping("/{discountId}/status")
    public Response<Void> updateStatus(@PathVariable String discountId, @RequestBody AdminStatusUpdateRequestDTO request) {
        discountDao.updateGroupBuyDiscountStatus(discountId, request.getStatus());
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }
}
