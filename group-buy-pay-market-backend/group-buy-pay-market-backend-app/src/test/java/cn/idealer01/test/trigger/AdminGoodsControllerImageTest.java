package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.AdminGoodsImageResponseDTO;
import cn.idealer01.api.dto.AdminGoodsListResponseDTO;
import cn.idealer01.api.dto.AdminGoodsUpsertRequestDTO;
import cn.idealer01.api.dto.AdminGoodsUpsertResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.ISkuDao;
import cn.idealer01.infrastructure.dao.ISkuImageDao;
import cn.idealer01.infrastructure.dao.po.Sku;
import cn.idealer01.infrastructure.dao.po.SkuImage;
import cn.idealer01.trigger.http.AdminGoodsController;
import cn.idealer01.trigger.http.AdminGoodsImageService;
import cn.idealer01.types.exception.AppException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.sql.SQLSyntaxErrorException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdminGoodsControllerImageTest {

    @Test
    public void constructor_marksSpringInjectionConstructor_whenMultipleConstructorsExist() throws Exception {
        Constructor<AdminGoodsController> constructor = AdminGoodsController.class.getConstructor(ISkuDao.class, ISkuImageDao.class, AdminGoodsImageService.class);

        assertNotNull(constructor.getAnnotation(Autowired.class));
    }

    @Test
    public void createGoods_returnsGeneratedGoodsId() {
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        AdminGoodsImageService imageService = mock(AdminGoodsImageService.class);
        when(skuDao.querySkuList()).thenReturn(Arrays.asList(Sku.builder().goodsId("9890004").build()));

        AdminGoodsController controller = new AdminGoodsController(skuDao, skuImageDao, imageService);
        Response<AdminGoodsUpsertResponseDTO> response = controller.createGoods(AdminGoodsUpsertRequestDTO.builder()
                .goodsName("新商品")
                .originalPrice(new BigDecimal("19.90"))
                .build());

        assertEquals("0000", response.getCode());
        assertEquals("9890005", response.getData().getGoodsId());
    }

    @Test
    public void queryGoodsList_returnsCoverAndImageList() {
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        AdminGoodsImageService imageService = mock(AdminGoodsImageService.class);
        when(skuDao.querySkuList()).thenReturn(Arrays.asList(Sku.builder().goodsId("9890001").goodsName("商品").originalPrice(new BigDecimal("9.90")).status(0).build()));
        when(skuImageDao.querySkuImagesByGoodsIds(Arrays.asList("9890001"))).thenReturn(Arrays.asList(
                SkuImage.builder().id(1L).goodsId("9890001").imageUrl("https://cdn.example.com/1.png").sortOrder(1).build(),
                SkuImage.builder().id(2L).goodsId("9890001").imageUrl("https://cdn.example.com/2.png").sortOrder(2).build()
        ));

        AdminGoodsController controller = new AdminGoodsController(skuDao, skuImageDao, imageService);
        Response<AdminGoodsListResponseDTO> response = controller.queryGoodsList();

        assertEquals("https://cdn.example.com/1.png", response.getData().getGoodsList().get(0).getCoverImageUrl());
        assertEquals(2, response.getData().getGoodsList().get(0).getImageList().size());
    }

    @Test
    public void queryGoodsList_returnsGoodsWithoutImages_whenSkuImageTableMissing() {
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        AdminGoodsImageService imageService = mock(AdminGoodsImageService.class);
        when(skuDao.querySkuList()).thenReturn(Collections.singletonList(Sku.builder().goodsId("9890001").goodsName("商品").originalPrice(new BigDecimal("9.90")).status(0).build()));
        when(skuImageDao.querySkuImagesByGoodsIds(Collections.singletonList("9890001")))
                .thenThrow(new BadSqlGrammarException("query", "select * from sku_image", new SQLSyntaxErrorException("Table 'sku_image' doesn't exist")));

        AdminGoodsController controller = new AdminGoodsController(skuDao, skuImageDao, imageService);
        Response<AdminGoodsListResponseDTO> response = controller.queryGoodsList();

        assertEquals("0000", response.getCode());
        assertEquals(1, response.getData().getGoodsList().size());
        assertEquals("9890001", response.getData().getGoodsList().get(0).getGoodsId());
        assertEquals(Collections.emptyList(), response.getData().getGoodsList().get(0).getImageList());
    }

    @Test
    public void uploadImage_delegatesToImageService() throws Exception {
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        AdminGoodsImageService imageService = mock(AdminGoodsImageService.class);
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});
        when(imageService.uploadImage("9890001", file)).thenReturn(AdminGoodsImageResponseDTO.builder().imageId(1L).imageUrl("u").sortOrder(1).build());

        AdminGoodsController controller = new AdminGoodsController(skuDao, skuImageDao, imageService);
        Response<AdminGoodsImageResponseDTO> response = controller.uploadImage("9890001", file);

        assertEquals(Long.valueOf(1L), response.getData().getImageId());
        verify(imageService).uploadImage("9890001", file);
    }

    @Test
    public void uploadImage_returnsBusinessError_whenImageServiceRejectsRequest() throws Exception {
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        AdminGoodsImageService imageService = mock(AdminGoodsImageService.class);
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});
        when(imageService.uploadImage("9890001", file)).thenThrow(new AppException("0002", "OSS配置缺失"));

        AdminGoodsController controller = new AdminGoodsController(skuDao, skuImageDao, imageService);
        Response<AdminGoodsImageResponseDTO> response = controller.uploadImage("9890001", file);

        assertEquals("0002", response.getCode());
        assertEquals("OSS配置缺失", response.getInfo());
    }

    @Test
    public void deleteImage_delegatesToImageService() {
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        AdminGoodsImageService imageService = mock(AdminGoodsImageService.class);

        AdminGoodsController controller = new AdminGoodsController(skuDao, skuImageDao, imageService);
        Response<Void> response = controller.deleteImage("9890001", 1L);

        assertEquals("0000", response.getCode());
        verify(imageService).deleteImage("9890001", 1L);
    }

    @Test
    public void deleteImage_returnsBusinessError_whenImageServiceRejectsRequest() {
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        AdminGoodsImageService imageService = mock(AdminGoodsImageService.class);
        doThrow(new AppException("0002", "OSS配置缺失")).when(imageService).deleteImage("9890001", 1L);

        AdminGoodsController controller = new AdminGoodsController(skuDao, skuImageDao, imageService);
        Response<Void> response = controller.deleteImage("9890001", 1L);

        assertEquals("0002", response.getCode());
        assertEquals("OSS配置缺失", response.getInfo());
    }
}
