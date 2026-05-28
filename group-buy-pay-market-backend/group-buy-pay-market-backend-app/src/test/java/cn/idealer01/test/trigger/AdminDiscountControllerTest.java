package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.AdminDiscountListResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.IGroupBuyActivityDao;
import cn.idealer01.infrastructure.dao.IGroupBuyDiscountDao;
import cn.idealer01.infrastructure.dao.po.GroupBuyDiscount;
import cn.idealer01.trigger.http.AdminDiscountController;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

public class AdminDiscountControllerTest {

    @Test
    public void queryDiscounts_returnsRows() {
        IGroupBuyDiscountDao discountDao = mock(IGroupBuyDiscountDao.class);
        when(discountDao.queryGroupBuyDiscountList()).thenReturn(Collections.singletonList(
                GroupBuyDiscount.builder().discountId(1).discountName("直减10元").marketPlan("ZJ").marketExpr("10").status(0).build()
        ));

        AdminDiscountController controller = new AdminDiscountController(discountDao, mock(IGroupBuyActivityDao.class));
        Response<AdminDiscountListResponseDTO> response = controller.queryDiscountList();

        assertEquals(1, response.getData().getDiscountList().size());
    }

    @Test
    public void createDiscount_generatesDiscountId_whenRequestOmitsIt() {
        IGroupBuyDiscountDao discountDao = mock(IGroupBuyDiscountDao.class);
        AdminDiscountController controller = new AdminDiscountController(discountDao, mock(IGroupBuyActivityDao.class));

        Response<Void> response = controller.createDiscount(
                cn.idealer01.api.dto.AdminDiscountUpsertRequestDTO.builder()
                        .discountName("直减20元")
                        .discountDesc("活动直减20元")
                        .discountType(1)
                        .marketPlan("ZJ")
                        .marketExpr("20")
                        .build()
        );

        ArgumentCaptor<GroupBuyDiscount> captor = ArgumentCaptor.forClass(GroupBuyDiscount.class);
        verify(discountDao).insertGroupBuyDiscount(captor.capture());
        assertNotNull(captor.getValue().getDiscountId());
        assertEquals("0000", response.getCode());
    }

    @Test
    public void createDiscount_rejectsUnsupportedMarketPlan() {
        IGroupBuyDiscountDao discountDao = mock(IGroupBuyDiscountDao.class);
        AdminDiscountController controller = new AdminDiscountController(discountDao, mock(IGroupBuyActivityDao.class));

        Response<Void> response = controller.createDiscount(
                cn.idealer01.api.dto.AdminDiscountUpsertRequestDTO.builder()
                        .discountName("未知类型")
                        .discountDesc("非法折扣")
                        .discountType(1)
                        .marketPlan("ABC")
                        .marketExpr("20")
                        .build()
        );

        assertEquals("0002", response.getCode());
        verify(discountDao, never()).insertGroupBuyDiscount(any());
    }
}
