package cn.idealer01.trigger.http;

import cn.idealer01.config.AdminAuthProperties;
import cn.idealer01.types.common.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AdminAuthProperties properties;

    public AdminAuthInterceptor(AdminAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("X-Admin-Token");
        String expected = Base64.getEncoder().encodeToString((properties.getUsername() + ":" + properties.getSecret())
                .getBytes(StandardCharsets.UTF_8));
        if (StringUtils.equals(token, expected)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"code\":\"" + Constants.ResponseCode.NO_LOGIN.getCode() + "\",\"info\":\"未登录\"}");
        return false;
    }
}
