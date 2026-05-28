package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.AdminGoodsListResponseDTO;
import cn.idealer01.api.dto.AdminGoodsImageResponseDTO;
import cn.idealer01.api.dto.AdminGoodsUpsertRequestDTO;
import cn.idealer01.api.dto.AdminGoodsUpsertResponseDTO;
import cn.idealer01.api.dto.AdminStatusUpdateRequestDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.ISkuDao;
import cn.idealer01.infrastructure.dao.ISkuImageDao;
import cn.idealer01.infrastructure.dao.po.Sku;
import cn.idealer01.infrastructure.dao.po.SkuImage;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/admin/goods")
public class AdminGoodsController {

    private final ISkuDao skuDao;
    private final ISkuImageDao skuImageDao;
    private final AdminGoodsImageService imageService;

    public AdminGoodsController(ISkuDao skuDao) {
        this(skuDao, null, null);
    }

    @Autowired
    public AdminGoodsController(ISkuDao skuDao, ISkuImageDao skuImageDao, AdminGoodsImageService imageService) {
        this.skuDao = skuDao;
        this.skuImageDao = skuImageDao;
        this.imageService = imageService;
    }

    @GetMapping
    public Response<AdminGoodsListResponseDTO> queryGoodsList() {
        List<Sku> skuList = skuDao.querySkuList();
        List<String> goodsIds = skuList.stream().map(Sku::getGoodsId).collect(Collectors.toList());
        List<SkuImage> imageList = queryImagesByGoodsIds(goodsIds);
        Map<String, List<SkuImage>> imagesByGoodsId = imageList.stream().collect(Collectors.groupingBy(SkuImage::getGoodsId));

        return Response.<AdminGoodsListResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(AdminGoodsListResponseDTO.builder()
                        .goodsList(skuList.stream().map(item -> buildGoodsItem(item, imagesByGoodsId.getOrDefault(item.getGoodsId(), Collections.emptyList()))).collect(Collectors.toList()))
                        .build())
                .build();
    }

    @PostMapping
    public Response<AdminGoodsUpsertResponseDTO> createGoods(@RequestBody AdminGoodsUpsertRequestDTO request) {
        if (request == null || StringUtils.isBlank(request.getGoodsName()) || request.getOriginalPrice() == null) {
            return Response.<AdminGoodsUpsertResponseDTO>builder()
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

        return Response.<AdminGoodsUpsertResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(AdminGoodsUpsertResponseDTO.builder().goodsId(goodsId).build())
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

    @PostMapping("/{goodsId}/images")
    public Response<AdminGoodsImageResponseDTO> uploadImage(@PathVariable String goodsId, @RequestParam("file") MultipartFile file) {
        try {
            AdminGoodsImageResponseDTO data = imageService.uploadImage(goodsId, file);
            return Response.<AdminGoodsImageResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (AppException e) {
            return Response.<AdminGoodsImageResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }
    }

    @DeleteMapping("/{goodsId}/images/{imageId}")
    public Response<Void> deleteImage(@PathVariable String goodsId, @PathVariable Long imageId) {
        try {
            imageService.deleteImage(goodsId, imageId);
            return Response.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (AppException e) {
            return Response.<Void>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }
    }

    private AdminGoodsListResponseDTO.GoodsItem buildGoodsItem(Sku item, List<SkuImage> images) {
        List<AdminGoodsListResponseDTO.GoodsImageItem> goodsImages = images.stream()
                .map(image -> AdminGoodsListResponseDTO.GoodsImageItem.builder()
                        .imageId(image.getId())
                        .imageUrl(image.getImageUrl())
                        .sortOrder(image.getSortOrder())
                        .build())
                .collect(Collectors.toList());
        return AdminGoodsListResponseDTO.GoodsItem.builder()
                .goodsId(item.getGoodsId())
                .goodsName(item.getGoodsName())
                .originalPrice(item.getOriginalPrice())
                .status(item.getStatus())
                .coverImageUrl(goodsImages.isEmpty() ? null : goodsImages.get(0).getImageUrl())
                .imageList(goodsImages)
                .build();
    }

    private List<SkuImage> queryImagesByGoodsIds(List<String> goodsIds) {
        if (skuImageDao == null || goodsIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return skuImageDao.querySkuImagesByGoodsIds(goodsIds);
        } catch (BadSqlGrammarException e) {
            return Collections.emptyList();
        }
    }
}
