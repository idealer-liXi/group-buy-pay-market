package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.AdminGoodsImageResponseDTO;
import cn.idealer01.infrastructure.dao.ISkuDao;
import cn.idealer01.infrastructure.dao.ISkuImageDao;
import cn.idealer01.infrastructure.dao.po.Sku;
import cn.idealer01.infrastructure.dao.po.SkuImage;
import cn.idealer01.infrastructure.oss.AliyunOssStorage;
import cn.idealer01.trigger.http.AdminGoodsImageService;
import cn.idealer01.types.exception.AppException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdminGoodsImageServiceTest {

    @Test
    public void uploadImage_rejectsMissingOssConfiguration() throws Exception {
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        AliyunOssStorage storage = mock(AliyunOssStorage.class);
        when(storage.isConfigured()).thenReturn(false);

        AdminGoodsImageService service = new AdminGoodsImageService(skuDao, skuImageDao, storage);

        try {
            service.uploadImage("9890001", new MockMultipartFile("file", "a.png", "image/png", new byte[]{1}));
            fail("Expected AppException");
        } catch (AppException e) {
            assertEquals("0002", e.getCode());
        }
    }

    @Test
    public void uploadImage_rejectsUnsupportedType() throws Exception {
        AdminGoodsImageService service = configuredService(mock(ISkuDao.class), mock(ISkuImageDao.class), mock(AliyunOssStorage.class));

        try {
            service.uploadImage("9890001", new MockMultipartFile("file", "a.txt", "text/plain", new byte[]{1}));
            fail("Expected AppException");
        } catch (AppException e) {
            assertEquals("0002", e.getCode());
        }
    }

    @Test
    public void uploadImage_insertsMetadataAndReturnsImage() throws Exception {
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        AliyunOssStorage storage = mock(AliyunOssStorage.class);
        when(storage.isConfigured()).thenReturn(true);
        when(storage.getDirPrefix()).thenReturn("goods/images");
        when(storage.buildPublicUrl(anyString())).thenReturn("https://cdn.example.com/goods/images/9890001/a.png");
        when(skuDao.querySkuByGoodsId("9890001")).thenReturn(Sku.builder().goodsId("9890001").goodsName("商品").originalPrice(new BigDecimal("9.90")).status(0).build());
        when(skuImageDao.queryMaxSortOrder("9890001")).thenReturn(2);
        when(skuImageDao.insertSkuImage(any(SkuImage.class))).thenAnswer(invocation -> {
            SkuImage skuImage = invocation.getArgument(0);
            skuImage.setId(9L);
            return 1;
        });

        AdminGoodsImageService service = new AdminGoodsImageService(skuDao, skuImageDao, storage);
        AdminGoodsImageResponseDTO response = service.uploadImage("9890001", new MockMultipartFile("file", "cover.png", "image/png", new byte[]{1, 2}));

        ArgumentCaptor<SkuImage> captor = ArgumentCaptor.forClass(SkuImage.class);
        verify(skuImageDao).insertSkuImage(captor.capture());
        assertEquals(Integer.valueOf(3), captor.getValue().getSortOrder());
        assertEquals(Long.valueOf(9L), response.getImageId());
        assertEquals("https://cdn.example.com/goods/images/9890001/a.png", response.getImageUrl());
    }

    @Test
    public void uploadImage_deletesOssObjectWhenMetadataInsertFails() throws Exception {
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        AliyunOssStorage storage = mock(AliyunOssStorage.class);
        when(storage.isConfigured()).thenReturn(true);
        when(storage.getDirPrefix()).thenReturn("goods/images");
        when(storage.buildPublicUrl(anyString())).thenReturn("https://cdn.example.com/a.png");
        when(skuDao.querySkuByGoodsId("9890001")).thenReturn(Sku.builder().goodsId("9890001").goodsName("商品").originalPrice(new BigDecimal("9.90")).status(0).build());
        when(skuImageDao.queryMaxSortOrder("9890001")).thenReturn(null);
        doThrow(new RuntimeException("db down")).when(skuImageDao).insertSkuImage(any(SkuImage.class));

        AdminGoodsImageService service = new AdminGoodsImageService(skuDao, skuImageDao, storage);

        try {
            service.uploadImage("9890001", new MockMultipartFile("file", "cover.png", "image/png", new byte[]{1, 2}));
            fail("Expected AppException");
        } catch (AppException e) {
            assertEquals("0001", e.getCode());
        }
        verify(storage).delete(anyString());
    }

    @Test
    public void uploadImage_doesNotDeleteOssObjectWhenUploadFails() throws Exception {
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        AliyunOssStorage storage = mock(AliyunOssStorage.class);
        when(storage.isConfigured()).thenReturn(true);
        when(storage.getDirPrefix()).thenReturn("goods/images");
        when(storage.buildPublicUrl(anyString())).thenReturn("https://cdn.example.com/a.png");
        when(skuDao.querySkuByGoodsId("9890001")).thenReturn(Sku.builder().goodsId("9890001").goodsName("商品").originalPrice(new BigDecimal("9.90")).status(0).build());
        doThrow(new RuntimeException("signature mismatch")).when(storage).upload(anyString(), any(), anyLong(), anyString());

        AdminGoodsImageService service = new AdminGoodsImageService(skuDao, skuImageDao, storage);

        try {
            service.uploadImage("9890001", new MockMultipartFile("file", "cover.png", "image/png", new byte[]{1, 2}));
            fail("Expected AppException");
        } catch (AppException e) {
            assertEquals("0001", e.getCode());
        }
        verify(storage, never()).delete(anyString());
    }

    @Test
    public void deleteImage_validatesOwnershipAndDeletesMetadataAfterOss() {
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        AliyunOssStorage storage = mock(AliyunOssStorage.class);
        when(storage.isConfigured()).thenReturn(true);
        when(skuImageDao.querySkuImageById("9890001", 7L)).thenReturn(SkuImage.builder().id(7L).goodsId("9890001").ossObjectKey("goods/images/9890001/a.png").build());

        AdminGoodsImageService service = new AdminGoodsImageService(skuDao, skuImageDao, storage);
        service.deleteImage("9890001", 7L);

        verify(storage).delete("goods/images/9890001/a.png");
        verify(skuImageDao).deleteSkuImage("9890001", 7L);
    }

    private AdminGoodsImageService configuredService(ISkuDao skuDao, ISkuImageDao skuImageDao, AliyunOssStorage storage) {
        when(storage.isConfigured()).thenReturn(true);
        return new AdminGoodsImageService(skuDao, skuImageDao, storage);
    }
}
