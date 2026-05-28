package cn.idealer01.test.config;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class ApplicationDevConfigTest {

    @Test
    public void mysqlJdbcTimezoneShouldMatchLocalMysqlContainerTimezone() throws Exception {
        String config = new String(Files.readAllBytes(Paths.get("src/main/resources/application-dev.yml")), StandardCharsets.UTF_8);

        assertTrue(config.contains("serverTimezone=Asia/Shanghai"));
    }
}
