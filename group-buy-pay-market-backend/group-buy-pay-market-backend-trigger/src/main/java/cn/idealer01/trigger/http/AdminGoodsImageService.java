package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.AdminGoodsImageResponseDTO;
import cn.idealer01.infrastructure.dao.ISkuDao;
import cn.idealer01.infrastructure.dao.ISkuImageDao;
import cn.idealer01.infrastructure.dao.po.SkuImage;
import cn.idealer01.infrastructure.oss.AliyunOssStorage;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
public class AdminGoodsImageService {
    private static final long MAX_FILE_SIZE = 20L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "webp"));

    private final ISkuDao skuDao;
    private final ISkuImageDao skuImageDao;
    private final AliyunOssStorage ossStorage;

    public AdminGoodsImageService(ISkuDao skuDao, ISkuImageDao skuImageDao, AliyunOssStorage ossStorage) {
        this.skuDao = skuDao;
        this.skuImageDao = skuImageDao;
        this.ossStorage = ossStorage;
    }

    public AdminGoodsImageResponseDTO uploadImage(String goodsId, MultipartFile file) {
        validateConfigured();
        validateGoods(goodsId);
        String extension = validateFile(file);

        Integer maxSortOrder = skuImageDao.queryMaxSortOrder(goodsId);
        int sortOrder = maxSortOrder == null ? 1 : maxSortOrder + 1;
        String objectKey = buildObjectKey(goodsId, extension);
        String imageUrl = ossStorage.buildPublicUrl(objectKey);
        boolean uploaded = false;

        try {
            ossStorage.upload(objectKey, file.getInputStream(), file.getSize(), file.getContentType());
            uploaded = true;
            SkuImage skuImage = SkuImage.builder()
                    .goodsId(goodsId)
                    .imageUrl(imageUrl)
                    .ossObjectKey(objectKey)
                    .sortOrder(sortOrder)
                    .build();
            skuImageDao.insertSkuImage(skuImage);
            return AdminGoodsImageResponseDTO.builder()
                    .imageId(skuImage.getId())
                    .imageUrl(imageUrl)
                    .sortOrder(sortOrder)
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("商品图片上传失败 goodsId:{} filename:{} objectKey:{}", goodsId, file.getOriginalFilename(), objectKey, e);
            if (uploaded) {
                try {
                    ossStorage.delete(objectKey);
                } catch (Exception cleanupException) {
                    log.warn("商品图片上传成功但保存失败，清理 OSS 对象失败 objectKey:{}", objectKey, cleanupException);
                }
            }
            throw new AppException(ResponseCode.UN_ERROR.getCode(), ResponseCode.UN_ERROR.getInfo());
        }
    }

    public void deleteImage(String goodsId, Long imageId) {
        validateConfigured();
        if (StringUtils.isBlank(goodsId) || imageId == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        SkuImage skuImage = skuImageDao.querySkuImageById(goodsId, imageId);
        if (skuImage == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        try {
            ossStorage.delete(skuImage.getOssObjectKey());
        } catch (Exception e) {
            log.error("商品图片删除 OSS 对象失败 goodsId:{} imageId:{} objectKey:{}", goodsId, imageId, skuImage.getOssObjectKey(), e);
            throw new AppException(ResponseCode.UN_ERROR.getCode(), ResponseCode.UN_ERROR.getInfo());
        }
        skuImageDao.deleteSkuImage(goodsId, imageId);
    }

    private void validateConfigured() {
        if (!ossStorage.isConfigured()) {
            log.warn("商品图片上传/删除失败：OSS 配置缺失");
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "OSS配置缺失");
        }
    }

    private void validateGoods(String goodsId) {
        if (StringUtils.isBlank(goodsId) || skuDao.querySkuByGoodsId(goodsId) == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
    }

    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.substringAfterLast(originalFilename, ".").toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        return extension;
    }

    private String buildObjectKey(String goodsId, String extension) {
        return ossStorage.getDirPrefix() + "/" + goodsId + "/" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + "-" + RandomStringUtils.randomAlphanumeric(8) + "." + extension;
    }
}
