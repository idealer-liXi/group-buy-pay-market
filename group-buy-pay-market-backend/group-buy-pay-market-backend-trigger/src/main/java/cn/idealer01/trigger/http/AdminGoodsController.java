package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.AdminGoodsListResponseDTO;
import cn.idealer01.api.dto.AdminGoodsUpsertRequestDTO;
import cn.idealer01.api.dto.AdminStatusUpdateRequestDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.ISkuDao;
import cn.idealer01.infrastructure.dao.po.Sku;
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

import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/admin/goods")
public class AdminGoodsController {

    private final ISkuDao skuDao;

    public AdminGoodsController(ISkuDao skuDao) {
        this.skuDao = skuDao;
    }

    @GetMapping
    public Response<AdminGoodsListResponseDTO> queryGoodsList() {
        return Response.<AdminGoodsListResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(AdminGoodsListResponseDTO.builder()
                        .goodsList(skuDao.querySkuList().stream().map(item -> AdminGoodsListResponseDTO.GoodsItem.builder()
                                .goodsId(item.getGoodsId())
                                .goodsName(item.getGoodsName())
                                .originalPrice(item.getOriginalPrice())
                                .status(item.getStatus())
                                .build()).collect(Collectors.toList()))
                        .build())
                .build();
    }

    @PostMapping
    public Response<Void> createGoods(@RequestBody AdminGoodsUpsertRequestDTO request) {
        if (request == null || StringUtils.isBlank(request.getGoodsName()) || request.getOriginalPrice() == null) {
            return Response.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                    .build();
        }

        String goodsId = StringUtils.isBlank(request.getGoodsId())
                ? String.valueOf(skuDao.querySkuList().stream()
                .map(Sku::getGoodsId)
                .filter(StringUtils::isNotBlank)
                .mapToLong(Long::parseLong)
                .max()
                .orElse(9890000L) + 1)
                : request.getGoodsId();

        skuDao.insertSku(Sku.builder()
                .source("s01")
                .channel("c01")
                .goodsId(goodsId)
                .goodsName(request.getGoodsName())
                .originalPrice(request.getOriginalPrice())
                .status(0)
                .build());

        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    @PutMapping("/{goodsId}")
    public Response<Void> updateGoods(@PathVariable String goodsId, @RequestBody AdminGoodsUpsertRequestDTO request) {
        skuDao.updateSku(Sku.builder()
                .goodsId(goodsId)
                .goodsName(request.getGoodsName())
                .originalPrice(request.getOriginalPrice())
                .build());
        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    @PutMapping("/{goodsId}/status")
    public Response<Void> updateStatus(@PathVariable String goodsId, @RequestBody AdminStatusUpdateRequestDTO request) {
        skuDao.updateSkuStatus(goodsId, request.getStatus());
        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }
}
