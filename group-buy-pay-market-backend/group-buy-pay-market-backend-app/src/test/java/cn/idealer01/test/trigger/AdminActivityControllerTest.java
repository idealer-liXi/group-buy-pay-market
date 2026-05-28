package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.AdminActivityListResponseDTO;
import cn.idealer01.api.dto.AdminActivityUpsertRequestDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.IGroupBuyActivityDao;
import cn.idealer01.infrastructure.dao.IGroupBuyDiscountDao;
import cn.idealer01.infrastructure.dao.ISCSkuActivityDao;
import cn.idealer01.infrastructure.dao.ISkuDao;
import cn.idealer01.infrastructure.dao.po.GroupBuyActivity;
import cn.idealer01.infrastructure.dao.po.GroupBuyDiscount;
import cn.idealer01.infrastructure.dao.po.SCSkuActivity;
import cn.idealer01.infrastructure.dao.po.Sku;
import cn.idealer01.trigger.http.AdminActivityController;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

public class AdminActivityControllerTest {

    @Test
    public void createActivity_writesActivityAndBinding() {
        IGroupBuyActivityDao activityDao = mock(IGroupBuyActivityDao.class);
        ISkuDao skuDao = mock(ISkuDao.class);
        IGroupBuyDiscountDao discountDao = mock(IGroupBuyDiscountDao.class);
        ISCSkuActivityDao scSkuActivityDao = mock(ISCSkuActivityDao.class);
        when(skuDao.querySkuByGoodsId("9890001")).thenReturn(
                Sku.builder().goodsId("9890001").goodsName("测试商品").originalPrice(new BigDecimal("10.00")).status(0).build()
        );
        when(discountDao.queryGroupBuyActivityDiscountByDiscountId("1")).thenReturn(
                GroupBuyDiscount.builder().discountId(1).discountName("直减10元").status(0).build()
        );
        AdminActivityController controller = new AdminActivityController(activityDao, skuDao, discountDao, scSkuActivityDao);

        AdminActivityUpsertRequestDTO request = AdminActivityUpsertRequestDTO.builder()
                .activityId(100999L)
                .activityName("测试活动")
                .goodsId("9890001")
                .discountId("1")
                .groupType(1)
                .takeLimitCount(1)
                .target(3)
                .validTime(15)
                .startTime(new Date())
                .endTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        Response<Void> response = controller.createActivity(request);

        verify(activityDao, times(1)).insertGroupBuyActivity(any());
        verify(scSkuActivityDao, times(1)).insertSCSkuActivity(any());
        assertEquals("0000", response.getCode());
    }

    @Test
    public void createActivity_writesOneBindingForEachCommaSeparatedGoodsId() {
        IGroupBuyActivityDao activityDao = mock(IGroupBuyActivityDao.class);
        ISkuDao skuDao = mock(ISkuDao.class);
        IGroupBuyDiscountDao discountDao = mock(IGroupBuyDiscountDao.class);
        ISCSkuActivityDao scSkuActivityDao = mock(ISCSkuActivityDao.class);
        when(skuDao.querySkuByGoodsId("9890001")).thenReturn(Sku.builder().goodsId("9890001").status(0).build());
        when(skuDao.querySkuByGoodsId("9890002")).thenReturn(Sku.builder().goodsId("9890002").status(0).build());
        when(discountDao.queryGroupBuyActivityDiscountByDiscountId("1")).thenReturn(GroupBuyDiscount.builder().discountId(1).status(0).build());
        AdminActivityController controller = new AdminActivityController(activityDao, skuDao, discountDao, scSkuActivityDao);

        AdminActivityUpsertRequestDTO request = AdminActivityUpsertRequestDTO.builder()
                .activityId(100999L)
                .activityName("测试活动")
                .goodsId("9890001,9890002")
                .discountId("1")
                .groupType(1)
                .takeLimitCount(1)
                .target(3)
                .validTime(15)
                .startTime(new Date())
                .endTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        Response<Void> response = controller.createActivity(request);

        assertEquals("0000", response.getCode());
        verify(scSkuActivityDao, times(2)).insertSCSkuActivity(any());
    }

    @Test
    public void createActivity_generatesActivityId_whenRequestOmitsIt() {
        IGroupBuyActivityDao activityDao = mock(IGroupBuyActivityDao.class);
        ISkuDao skuDao = mock(ISkuDao.class);
        IGroupBuyDiscountDao discountDao = mock(IGroupBuyDiscountDao.class);
        ISCSkuActivityDao scSkuActivityDao = mock(ISCSkuActivityDao.class);
        when(skuDao.querySkuByGoodsId("9890001")).thenReturn(
                Sku.builder().goodsId("9890001").goodsName("测试商品").originalPrice(new BigDecimal("10.00")).status(0).build()
        );
        when(discountDao.queryGroupBuyActivityDiscountByDiscountId("1")).thenReturn(
                GroupBuyDiscount.builder().discountId(1).discountName("直减10元").status(0).build()
        );
        when(scSkuActivityDao.querySCSkuActivityBySCGoodsId(any())).thenReturn(null);

        AdminActivityController controller = new AdminActivityController(activityDao, skuDao, discountDao, scSkuActivityDao);
        AdminActivityUpsertRequestDTO request = AdminActivityUpsertRequestDTO.builder()
                .activityName("测试活动")
                .goodsId("9890001")
                .discountId("1")
                .groupType(1)
                .takeLimitCount(1)
                .target(3)
                .validTime(15)
                .startTime(new Date())
                .endTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        Response<Void> response = controller.createActivity(request);

        ArgumentCaptor<GroupBuyActivity> activityCaptor = ArgumentCaptor.forClass(GroupBuyActivity.class);
        verify(activityDao).insertGroupBuyActivity(activityCaptor.capture());
        assertNotNull(activityCaptor.getValue().getActivityId());
        assertEquals("0000", response.getCode());
    }

    @Test
    public void createActivity_rejectsGoodsAlreadyBoundToAnotherActivity() {
        IGroupBuyActivityDao activityDao = mock(IGroupBuyActivityDao.class);
        ISkuDao skuDao = mock(ISkuDao.class);
        IGroupBuyDiscountDao discountDao = mock(IGroupBuyDiscountDao.class);
        ISCSkuActivityDao scSkuActivityDao = mock(ISCSkuActivityDao.class);
        when(skuDao.querySkuByGoodsId("9890001")).thenReturn(
                Sku.builder().goodsId("9890001").goodsName("测试商品").originalPrice(new BigDecimal("10.00")).status(0).build()
        );
        when(discountDao.queryGroupBuyActivityDiscountByDiscountId("1")).thenReturn(
                GroupBuyDiscount.builder().discountId(1).discountName("直减10元").status(0).build()
        );
        when(scSkuActivityDao.querySCSkuActivityBySCGoodsId(any())).thenReturn(
                SCSkuActivity.builder().activityId(100123L).goodsId("9890001").source("s01").channel("c01").build()
        );

        AdminActivityController controller = new AdminActivityController(activityDao, skuDao, discountDao, scSkuActivityDao);
        AdminActivityUpsertRequestDTO request = AdminActivityUpsertRequestDTO.builder()
                .activityId(100999L)
                .activityName("测试活动")
                .goodsId("9890001").discountId("1")
                .groupType(1).takeLimitCount(1).target(3).validTime(15)
                .startTime(new Date())
                .endTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        Response<Void> response = controller.createActivity(request);

        assertEquals("0002", response.getCode());
        assertEquals("商品已绑定其他活动", response.getInfo());
        verify(activityDao, never()).insertGroupBuyActivity(any());
    }

    @Test
    public void queryActivityList_includesBoundGoodsId() {
        IGroupBuyActivityDao activityDao = mock(IGroupBuyActivityDao.class);
        ISCSkuActivityDao scSkuActivityDao = mock(ISCSkuActivityDao.class);
        when(activityDao.queryGroupBuyActivityList()).thenReturn(Collections.singletonList(
                GroupBuyActivity.builder().activityId(100123L).activityName("拼团读书节").discountId("1").status(1).build()
        ));
        when(scSkuActivityDao.querySCSkuActivityListByActivityId(100123L)).thenReturn(Arrays.asList(
                SCSkuActivity.builder().activityId(100123L).goodsId("9890001").source("s01").channel("c01").build(),
                SCSkuActivity.builder().activityId(100123L).goodsId("9890002").source("s01").channel("c01").build()
        ));

        AdminActivityController controller = new AdminActivityController(activityDao, mock(ISkuDao.class), mock(IGroupBuyDiscountDao.class), scSkuActivityDao);
        Response<AdminActivityListResponseDTO> response = controller.queryActivityList();

        assertEquals("9890001,9890002", response.getData().getActivityList().get(0).getGoodsId());
    }

    @Test
    public void createActivity_rejectsDisabledGoods() {
        IGroupBuyActivityDao activityDao = mock(IGroupBuyActivityDao.class);
        ISkuDao skuDao = mock(ISkuDao.class);
        IGroupBuyDiscountDao discountDao = mock(IGroupBuyDiscountDao.class);
        ISCSkuActivityDao scSkuActivityDao = mock(ISCSkuActivityDao.class);
        when(skuDao.querySkuByGoodsId("9890001")).thenReturn(
                Sku.builder().goodsId("9890001").status(1).build()
        );
        when(discountDao.queryGroupBuyActivityDiscountByDiscountId("1")).thenReturn(
                GroupBuyDiscount.builder().discountId(1).status(0).build()
        );

        AdminActivityController controller = new AdminActivityController(activityDao, skuDao, discountDao, scSkuActivityDao);
        AdminActivityUpsertRequestDTO request = AdminActivityUpsertRequestDTO.builder()
                .activityId(100999L)
                .activityName("测试活动")
                .goodsId("9890001")
                .discountId("1")
                .groupType(1)
                .takeLimitCount(1)
                .target(3)
                .validTime(15)
                .startTime(new Date())
                .endTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        Response<Void> response = controller.createActivity(request);

        assertEquals("0002", response.getCode());
    }

    @Test
    public void updateActivity_rebindsByActivityId_andRejectsDisabledDiscount() {
        IGroupBuyActivityDao activityDao = mock(IGroupBuyActivityDao.class);
        ISkuDao skuDao = mock(ISkuDao.class);
        IGroupBuyDiscountDao discountDao = mock(IGroupBuyDiscountDao.class);
        ISCSkuActivityDao scSkuActivityDao = mock(ISCSkuActivityDao.class);

        when(activityDao.queryGroupBuyActivityByActivityId(100123L)).thenReturn(
                GroupBuyActivity.builder().activityId(100123L).status(0).build()
        );
        when(skuDao.querySkuByGoodsId("9890002")).thenReturn(
                Sku.builder().goodsId("9890002").status(0).build()
        );
        when(discountDao.queryGroupBuyActivityDiscountByDiscountId("2")).thenReturn(
                GroupBuyDiscount.builder().discountId(2).status(0).build()
        );

        AdminActivityController controller = new AdminActivityController(activityDao, skuDao, discountDao, scSkuActivityDao);
        AdminActivityUpsertRequestDTO request = AdminActivityUpsertRequestDTO.builder()
                .activityName("测试活动2")
                .goodsId("9890002")
                .discountId("2")
                .groupType(1)
                .takeLimitCount(1)
                .target(3)
                .validTime(15)
                .startTime(new Date())
                .endTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        Response<Void> response = controller.updateActivity(100123L, request);

        verify(scSkuActivityDao, times(1)).updateSCSkuActivityByActivityId(eq(100123L), eq("9890002"), eq("s01"), eq("c01"));
        assertEquals("0000", response.getCode());
    }

    @Test
    public void updateActivity_replacesBindingsWhenGoodsIdContainsMultipleIds() {
        IGroupBuyActivityDao activityDao = mock(IGroupBuyActivityDao.class);
        ISkuDao skuDao = mock(ISkuDao.class);
        IGroupBuyDiscountDao discountDao = mock(IGroupBuyDiscountDao.class);
        ISCSkuActivityDao scSkuActivityDao = mock(ISCSkuActivityDao.class);
        when(activityDao.queryGroupBuyActivityByActivityId(100123L)).thenReturn(
                GroupBuyActivity.builder().activityId(100123L).status(0).build()
        );
        when(skuDao.querySkuByGoodsId("9890001")).thenReturn(Sku.builder().goodsId("9890001").status(0).build());
        when(skuDao.querySkuByGoodsId("9890002")).thenReturn(Sku.builder().goodsId("9890002").status(0).build());
        when(discountDao.queryGroupBuyActivityDiscountByDiscountId("1")).thenReturn(GroupBuyDiscount.builder().discountId(1).status(0).build());
        when(scSkuActivityDao.querySCSkuActivityBySCGoodsId(any())).thenReturn(
                SCSkuActivity.builder().activityId(100123L).build()
        );

        AdminActivityController controller = new AdminActivityController(activityDao, skuDao, discountDao, scSkuActivityDao);
        AdminActivityUpsertRequestDTO request = AdminActivityUpsertRequestDTO.builder()
                .activityName("测试活动")
                .goodsId("9890001,9890002")
                .discountId("1")
                .groupType(1)
                .takeLimitCount(1)
                .target(3)
                .validTime(15)
                .startTime(new Date())
                .endTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        Response<Void> response = controller.updateActivity(100123L, request);

        assertEquals("0000", response.getCode());
        verify(scSkuActivityDao, times(1)).deleteSCSkuActivityByActivityId(100123L);
        verify(scSkuActivityDao, times(2)).insertSCSkuActivity(any());
    }
}
