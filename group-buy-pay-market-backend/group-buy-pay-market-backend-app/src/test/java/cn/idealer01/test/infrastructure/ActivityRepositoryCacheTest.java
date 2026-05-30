package cn.idealer01.test.infrastructure;

import cn.idealer01.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.idealer01.infrastructure.adapter.repository.AbstractRepository;
import cn.idealer01.infrastructure.adapter.repository.ActivityRepository;
import cn.idealer01.infrastructure.dao.IGroupBuyActivityDao;
import cn.idealer01.infrastructure.dao.IGroupBuyDiscountDao;
import cn.idealer01.infrastructure.dao.po.GroupBuyActivity;
import cn.idealer01.infrastructure.dao.po.GroupBuyDiscount;
import cn.idealer01.infrastructure.dcc.DCCService;
import cn.idealer01.infrastructure.redis.IRedisService;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActivityRepositoryCacheTest {

    @Test
    public void queryGroupBuyActivityDiscountVO_doesNotUseStaleActiveActivityCacheWhenActivityIsDisabled() {
        ActivityRepository repository = new ActivityRepository();
        IGroupBuyActivityDao activityDao = mock(IGroupBuyActivityDao.class);
        IGroupBuyDiscountDao discountDao = mock(IGroupBuyDiscountDao.class);
        IRedisService redisService = mock(IRedisService.class);
        DCCService dccService = mock(DCCService.class);

        when(dccService.isCacheOpenSwitch()).thenReturn(true);
        when(redisService.getValue(GroupBuyActivity.cacheRedisKey(9890001L))).thenReturn(GroupBuyActivity.builder()
                .activityId(9890001L)
                .activityName("stale-active")
                .discountId("3")
                .status(1)
                .build());
        when(activityDao.queryValidGroupBuyActivityId(9890001L)).thenReturn(null);
        when(discountDao.queryGroupBuyActivityDiscountByDiscountId("3")).thenReturn(GroupBuyDiscount.builder()
                .discountId(3)
                .discountName("直减优惠")
                .discountType(1)
                .marketPlan("ZJ")
                .marketExpr("10")
                .status(0)
                .build());

        ReflectionTestUtils.setField(repository, "groupBuyActivityDao", activityDao);
        ReflectionTestUtils.setField(repository, "groupBuyDiscountDao", discountDao);
        ReflectionTestUtils.setField(repository, AbstractRepository.class, "redisService", redisService, IRedisService.class);
        ReflectionTestUtils.setField(repository, AbstractRepository.class, "dccService", dccService, DCCService.class);

        GroupBuyActivityDiscountVO result = repository.queryGroupBuyActivityDiscountVO(9890001L);

        assertNull(result);
    }
}
