package cn.idealer01.test;

import cn.idealer01.config.AliPayConfig;
import cn.idealer01.config.GuavaConfig;
import cn.idealer01.config.ThreadPoolConfig;
import cn.idealer01.config.WeixinApiConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationSmokeTest.TestApplication.class, properties = {
        "alipay.enabled=false",
        "spring.autoconfigure.exclude=cn.idealer.wrench.dynamic.config.center.config.DynamicConfigCenterAutoConfig,cn.idealer.wrench.dynamic.config.center.config.DynamicConfigCenterRegisterAutoConfig,org.redisson.spring.starter.RedissonAutoConfiguration,org.redisson.spring.starter.RedissonAutoConfigurationV2"
})
public class ApplicationSmokeTest {

    @Test
    public void contextLoads() {
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({ThreadPoolConfig.class, GuavaConfig.class, WeixinApiConfig.class, AliPayConfig.class})
    static class TestApplication {
    }
}
