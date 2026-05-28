package cn.idealer01.test.config;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApplicationDevConfigTest {

    @Test
    public void mysqlJdbcTimezoneShouldMatchLocalMysqlContainerTimezone() throws Exception {
        String config = new String(Files.readAllBytes(Paths.get("src/main/resources/application-dev.yml")), StandardCharsets.UTF_8);

        assertTrue(config.contains("serverTimezone=Asia/Shanghai"));
    }

    @Test
    public void adminAuthShouldLiveInProfileConfigsNotBaseConfig() throws Exception {
        String baseConfig = readConfig("application.yml");
        String devConfig = readConfig("application-dev.yml");
        String testConfig = readConfig("application-test.yml");
        String prodConfig = readConfig("application-prod.yml");

        assertFalse(baseConfig.contains("admin:"));
        assertTrue(devConfig.contains("admin:"));
        assertTrue(devConfig.contains("auth:"));
        assertTrue(testConfig.contains("admin:"));
        assertTrue(testConfig.contains("auth:"));
        assertTrue(prodConfig.contains("admin:"));
        assertTrue(prodConfig.contains("auth:"));
    }

    @Test
    public void alipayDevConfigShouldUseCanonicalKebabCaseKeys() throws Exception {
        String config = readConfig("application-dev.yml");

        assertTrue(config.contains("notify-url:"));
        assertTrue(config.contains("return-url:"));
        assertTrue(config.contains("gateway-url:"));
        assertTrue(config.contains("app-id:"));
        assertTrue(config.contains("merchant-private-key:"));
        assertTrue(config.contains("alipay-public-key:"));
        assertFalse(config.contains("notify_url:"));
        assertFalse(config.contains("return_url:"));
        assertFalse(config.contains("app_id:"));
        assertFalse(config.contains("merchant_private_key:"));
        assertFalse(config.contains("alipay_public_key:"));
    }

    @Test
    public void ossDevConfigShouldUseBindablePublicBaseUrlKey() throws Exception {
        String config = readConfig("application-dev.yml");

        assertTrue(config.contains("public-base-url:"));
        assertFalse(config.contains("public-domain:"));
    }

    private String readConfig(String fileName) throws Exception {
        return new String(Files.readAllBytes(Paths.get("src/main/resources", fileName)), StandardCharsets.UTF_8);
    }
}
