package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.AdminGoodsListResponseDTO;
import cn.idealer01.api.dto.AdminGoodsUpsertResponseDTO;
import cn.idealer01.api.dto.AdminStatusUpdateRequestDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.ISkuDao;
import cn.idealer01.infrastructure.dao.po.Sku;
import cn.idealer01.trigger.http.AdminGoodsController;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

public class AdminGoodsControllerTest {

    @Test
    public void list_returnsMappedGoodsRows() {
        ISkuDao skuDao = mock(ISkuDao.class);
        when(skuDao.querySkuList()).thenReturn(Collections.singletonList(
                Sku.builder().goodsId("9890001").goodsName("test").originalPrice(new BigDecimal("10.00")).status(0).build()
        ));

        AdminGoodsController controller = new AdminGoodsController(skuDao);
        Response<AdminGoodsListResponseDTO> response = controller.queryGoodsList();

        assertEquals(1, response.getData().getGoodsList().size());
        assertEquals("9890001", response.getData().getGoodsList().get(0).getGoodsId());
    }

    @Test
    public void updateStatus_returnsSuccess_whenDaoUpdatesOneRow() {
        ISkuDao skuDao = mock(ISkuDao.class);
        when(skuDao.updateSkuStatus("9890001", 1)).thenReturn(1);
        when(skuDao.querySkuList()).thenReturn(Collections.singletonList(
                Sku.builder().goodsId("9890001").goodsName("test").originalPrice(new BigDecimal("10.00")).status(0).build()
        ));

        AdminGoodsController controller = new AdminGoodsController(skuDao);
        Response<Void> response = controller.updateStatus("9890001", new AdminStatusUpdateRequestDTO(1));

        assertEquals("0000", response.getCode());
    }

    @Test
    public void createGoods_generatesGoodsId_whenRequestOmitsIt() {
        ISkuDao skuDao = mock(ISkuDao.class);
        AdminGoodsController controller = new AdminGoodsController(skuDao);

        Response<AdminGoodsUpsertResponseDTO> response = controller.createGoods(
                cn.idealer01.api.dto.AdminGoodsUpsertRequestDTO.builder()
                        .goodsName("新商品")
                        .originalPrice(new BigDecimal("19.90"))
                        .build()
        );

        ArgumentCaptor<Sku> captor = ArgumentCaptor.forClass(Sku.class);
        verify(skuDao).insertSku(captor.capture());
        assertNotNull(captor.getValue().getGoodsId());
        assertEquals(captor.getValue().getGoodsId(), response.getData().getGoodsId());
        assertEquals("0000", response.getCode());
    }
}
