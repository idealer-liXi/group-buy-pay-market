package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.AdminLoginRequestDTO;
import cn.idealer01.api.dto.AdminLoginResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.config.AdminAuthProperties;
import cn.idealer01.types.common.Constants;
import cn.idealer01.types.enums.ResponseCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/admin")
public class AdminAuthController {

    private final AdminAuthProperties properties;

    public AdminAuthController(AdminAuthProperties properties) {
        this.properties = properties;
    }

    @PostMapping("/login")
    public Response<AdminLoginResponseDTO> login(@RequestBody AdminLoginRequestDTO request) {
        if (request == null || StringUtils.isBlank(request.getUsername()) || StringUtils.isBlank(request.getPassword())) {
            return Response.<AdminLoginResponseDTO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                    .build();
        }

        boolean matched = StringUtils.equals(properties.getUsername(), request.getUsername())
                && StringUtils.equals(properties.getPassword(), request.getPassword());
        if (!matched) {
            return Response.<AdminLoginResponseDTO>builder()
                    .code(Constants.ResponseCode.NO_LOGIN.getCode())
                    .info("账号或密码错误")
                    .build();
        }

        String raw = request.getUsername() + ":" + properties.getSecret();
        String token = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return Response.<AdminLoginResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(AdminLoginResponseDTO.builder()
                        .adminToken(token)
                        .displayName("管理员")
                        .build())
                .build();
    }
}
