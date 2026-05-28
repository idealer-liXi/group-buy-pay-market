package cn.idealer01.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "admin.auth")
public class AdminAuthProperties {
    private String username;
    private String password;
    private String secret;
}
