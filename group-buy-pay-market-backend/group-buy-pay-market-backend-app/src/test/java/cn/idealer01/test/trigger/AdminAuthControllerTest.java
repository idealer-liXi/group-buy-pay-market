package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.AdminLoginRequestDTO;
import cn.idealer01.api.dto.AdminLoginResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.config.AdminAuthProperties;
import cn.idealer01.trigger.http.AdminAuthController;
import cn.idealer01.trigger.http.AdminAuthInterceptor;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AdminAuthControllerTest {

    @Test
    public void login_returnsToken_forConfiguredCredentials() {
        AdminAuthProperties properties = new AdminAuthProperties();
        properties.setUsername("admin");
        properties.setPassword("123456");
        properties.setSecret("admin-secret");

        AdminAuthController controller = new AdminAuthController(properties);

        AdminLoginRequestDTO request = AdminLoginRequestDTO.builder()
                .username("admin")
                .password("123456")
                .build();

        Response<AdminLoginResponseDTO> response = controller.login(request);

        assertEquals("0000", response.getCode());
        assertNotNull(response.getData().getAdminToken());
    }

    @Test
    public void preflightOptionsRequest_isAllowedWithoutToken() throws Exception {
        AdminAuthProperties properties = new AdminAuthProperties();
        properties.setUsername("admin");
        properties.setPassword("123456");
        properties.setSecret("admin-secret");

        AdminAuthInterceptor interceptor = new AdminAuthInterceptor(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/v1/admin/goods");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
    }
}
