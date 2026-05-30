package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.AdminTagListResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.ICrowdTagsDao;
import cn.idealer01.infrastructure.dao.ICrowdTagsDetailDao;
import cn.idealer01.infrastructure.dao.po.CrowdTags;
import cn.idealer01.infrastructure.dao.po.CrowdTagsDetail;
import cn.idealer01.trigger.http.UserProfileController;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserProfileControllerTest {

    @Test
    public void queryUserTags_returnsCurrentUserTags() {
        ICrowdTagsDetailDao detailDao = mock(ICrowdTagsDetailDao.class);
        ICrowdTagsDao tagsDao = mock(ICrowdTagsDao.class);
        when(detailDao.queryCrowdTagsDetailListByUserId("u1")).thenReturn(Collections.singletonList(
                CrowdTagsDetail.builder().tagId("T001").userId("u1").build()
        ));
        when(tagsDao.queryCrowdTagsByTagId("T001")).thenReturn(CrowdTags.builder()
                .tagId("T001")
                .tagName("新人")
                .tagDesc("新人标签")
                .statistics(1)
                .build());

        UserProfileController controller = new UserProfileController(detailDao, tagsDao);
        Response<AdminTagListResponseDTO> response = controller.queryUserTags("u1");

        assertEquals("0000", response.getCode());
        assertEquals(1, response.getData().getTagList().size());
        assertEquals("T001", response.getData().getTagList().get(0).getTagId());
        assertEquals("新人", response.getData().getTagList().get(0).getTagName());
    }
}
