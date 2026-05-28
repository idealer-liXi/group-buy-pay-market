package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.AdminTagMemberRequestDTO;
import cn.idealer01.api.dto.AdminTagUpsertRequestDTO;
import cn.idealer01.infrastructure.dao.ICrowdTagsDao;
import cn.idealer01.infrastructure.dao.ICrowdTagsDetailDao;
import cn.idealer01.infrastructure.dao.IMarketUserDao;
import cn.idealer01.infrastructure.dao.po.CrowdTags;
import cn.idealer01.infrastructure.dao.po.MarketUser;
import cn.idealer01.infrastructure.redis.IRedisService;
import cn.idealer01.trigger.http.AdminTagController;
import org.junit.Test;
import org.redisson.api.RBitSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdminTagControllerTest {

    @Test
    public void createsTag() {
        AdminTagController controller = newController();
        AdminTagUpsertRequestDTO request = new AdminTagUpsertRequestDTO();
        request.setTagId("T001");
        request.setTagName("新人");
        request.setTagDesc("新人标签");

        assertEquals("0000", controller.createTag(request).getCode());
    }

    @Test
    public void addsTagMemberAndSetsBitSet() {
        ICrowdTagsDao tagsDao = mock(ICrowdTagsDao.class);
        ICrowdTagsDetailDao detailDao = mock(ICrowdTagsDetailDao.class);
        IMarketUserDao userDao = mock(IMarketUserDao.class);
        IRedisService redisService = mock(IRedisService.class);
        RBitSet bitSet = mock(RBitSet.class);
        when(tagsDao.queryCrowdTagsByTagId("T001")).thenReturn(CrowdTags.builder().tagId("T001").build());
        when(userDao.queryMarketUserByUserId("u1")).thenReturn(MarketUser.builder().userId("u1").build());
        when(redisService.getBitSet("T001")).thenReturn(bitSet);
        when(redisService.getIndexFromUserId("u1")).thenReturn(7);
        when(detailDao.countCrowdTagsDetailByTagId("T001")).thenReturn(1);
        AdminTagController controller = new AdminTagController(tagsDao, detailDao, userDao, redisService);

        AdminTagMemberRequestDTO request = new AdminTagMemberRequestDTO();
        request.setUserId("u1");

        assertEquals("0000", controller.addMember("T001", request).getCode());
        verify(detailDao).addCrowdTagsUserId(any());
        verify(bitSet).set(7, true);
        verify(tagsDao).updateCrowdTagsStatisticsTo("T001", 1);
    }

    @Test
    public void removesTagMemberAndClearsBitSet() {
        ICrowdTagsDao tagsDao = mock(ICrowdTagsDao.class);
        ICrowdTagsDetailDao detailDao = mock(ICrowdTagsDetailDao.class);
        IMarketUserDao userDao = mock(IMarketUserDao.class);
        IRedisService redisService = mock(IRedisService.class);
        RBitSet bitSet = mock(RBitSet.class);
        when(redisService.getBitSet("T001")).thenReturn(bitSet);
        when(redisService.getIndexFromUserId("u1")).thenReturn(7);
        when(detailDao.countCrowdTagsDetailByTagId("T001")).thenReturn(0);
        AdminTagController controller = new AdminTagController(tagsDao, detailDao, userDao, redisService);

        assertEquals("0000", controller.removeMember("T001", "u1").getCode());
        verify(detailDao).deleteCrowdTagsUserId("T001", "u1");
        verify(bitSet).set(7, false);
        verify(tagsDao).updateCrowdTagsStatisticsTo("T001", 0);
    }

    private AdminTagController newController() {
        return new AdminTagController(mock(ICrowdTagsDao.class), mock(ICrowdTagsDetailDao.class), mock(IMarketUserDao.class), mock(IRedisService.class));
    }
}
