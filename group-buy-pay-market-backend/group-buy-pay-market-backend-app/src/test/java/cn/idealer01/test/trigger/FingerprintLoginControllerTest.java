package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.FingerprintLoginRequestDTO;
import cn.idealer01.api.dto.FingerprintLoginResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.IMarketUserDao;
import cn.idealer01.infrastructure.dao.po.MarketUser;
import cn.idealer01.trigger.http.FingerprintLoginController;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FingerprintLoginControllerTest {

    @Test
    public void createsFingerprintUserWhenMissing() {
        IMarketUserDao userDao = mock(IMarketUserDao.class);
        FingerprintLoginController controller = new FingerprintLoginController(userDao);

        FingerprintLoginRequestDTO request = new FingerprintLoginRequestDTO();
        request.setVisitorId("visitor-123456");

        Response<FingerprintLoginResponseDTO> response = controller.loginByFingerprint(request);

        assertEquals("0000", response.getCode());
        assertEquals("visitor-123456", response.getData().getUserId());
        assertNotNull(response.getData().getDisplayName());
        verify(userDao).insertMarketUser(any(MarketUser.class));
    }

    @Test
    public void updatesLastLoginTimeWhenUserExists() {
        IMarketUserDao userDao = mock(IMarketUserDao.class);
        when(userDao.queryMarketUserByUserId("visitor-123456")).thenReturn(MarketUser.builder()
                .userId("visitor-123456")
                .displayName("指纹用户-123456")
                .build());
        FingerprintLoginController controller = new FingerprintLoginController(userDao);

        FingerprintLoginRequestDTO request = new FingerprintLoginRequestDTO();
        request.setVisitorId("visitor-123456");

        Response<FingerprintLoginResponseDTO> response = controller.loginByFingerprint(request);

        assertEquals("0000", response.getCode());
        assertEquals("visitor-123456", response.getData().getUserId());
        verify(userDao).updateMarketUserLastLoginTime("visitor-123456");
    }

    @Test
    public void rejectsBlankVisitorId() {
        IMarketUserDao userDao = mock(IMarketUserDao.class);
        FingerprintLoginController controller = new FingerprintLoginController(userDao);

        FingerprintLoginRequestDTO request = new FingerprintLoginRequestDTO();
        request.setVisitorId(" ");

        Response<FingerprintLoginResponseDTO> response = controller.loginByFingerprint(request);

        assertEquals("0002", response.getCode());
    }
}
