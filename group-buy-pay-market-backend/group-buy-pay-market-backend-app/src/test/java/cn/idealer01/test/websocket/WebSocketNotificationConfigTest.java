package cn.idealer01.test.websocket;

import cn.idealer01.trigger.websocket.WebSocketNotificationConfig;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class WebSocketNotificationConfigTest {

    @Test
    public void allowedOrigins_includePublicFrpFrontend() {
        WebSocketNotificationConfig config = new WebSocketNotificationConfig();

        assertTrue(Arrays.asList(config.allowedOrigins()).contains("http://110.42.207.45:15173"));
    }
}
