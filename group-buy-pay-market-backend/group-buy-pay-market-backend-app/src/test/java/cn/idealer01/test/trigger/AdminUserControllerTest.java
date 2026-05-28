package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.AdminUserListResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.ICrowdTagsDao;
import cn.idealer01.infrastructure.dao.ICrowdTagsDetailDao;
import cn.idealer01.infrastructure.dao.IMarketUserDao;
import cn.idealer01.infrastructure.dao.po.CrowdTags;
import cn.idealer01.infrastructure.dao.po.CrowdTagsDetail;
import cn.idealer01.infrastructure.dao.po.MarketUser;
import cn.idealer01.trigger.http.AdminUserController;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdminUserControllerTest {

    @Test
    public void listsUsers() {
        IMarketUserDao userDao = mock(IMarketUserDao.class);
        ICrowdTagsDetailDao detailDao = mock(ICrowdTagsDetailDao.class);
        ICrowdTagsDao tagsDao = mock(ICrowdTagsDao.class);
        when(userDao.queryMarketUserList("u1")).thenReturn(Collections.singletonList(MarketUser.builder()
                .userId("u1")
                .displayName("指纹用户-u1")
                .loginType("FINGERPRINT")
                .status(0)
                .build()));
        AdminUserController controller = new AdminUserController(userDao, detailDao, tagsDao);

        Response<AdminUserListResponseDTO> response = controller.queryUsers("u1");

        assertEquals("0000", response.getCode());
        assertEquals("u1", response.getData().getUserList().get(0).getUserId());
    }

    @Test
    public void listsUserTags() {
        IMarketUserDao userDao = mock(IMarketUserDao.class);
        ICrowdTagsDetailDao detailDao = mock(ICrowdTagsDetailDao.class);
        ICrowdTagsDao tagsDao = mock(ICrowdTagsDao.class);
        when(detailDao.queryCrowdTagsDetailListByUserId("u1")).thenReturn(Collections.singletonList(CrowdTagsDetail.builder()
                .tagId("T001")
                .userId("u1")
                .build()));
        when(tagsDao.queryCrowdTagsByTagId("T001")).thenReturn(CrowdTags.builder()
                .tagId("T001")
                .tagName("新人")
                .tagDesc("新人标签")
                .statistics(1)
                .build());
        AdminUserController controller = new AdminUserController(userDao, detailDao, tagsDao);

        assertEquals("T001", controller.queryUserTags("u1").getData().getTagList().get(0).getTagId());
    }
}
