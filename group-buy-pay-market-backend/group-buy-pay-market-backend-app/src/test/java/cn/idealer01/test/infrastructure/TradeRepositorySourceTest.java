package cn.idealer01.test.infrastructure;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;

public class TradeRepositorySourceTest {

    @Test
    public void tradeRepositoryDoesNotUseAnonymousHashMapClassesForNotifyPayloads() throws Exception {
        String source = new String(Files.readAllBytes(Paths.get("..", "group-buy-pay-market-backend-infrastructure", "src", "main", "java", "cn", "idealer01", "infrastructure", "adapter", "repository", "TradeRepository.java")), StandardCharsets.UTF_8);

        assertFalse(source.contains("new HashMap<String, Object>(){"));
    }
}
