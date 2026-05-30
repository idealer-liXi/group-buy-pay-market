package cn.idealer01.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliPayConfigProperties.class)
@ConditionalOnProperty(value = "alipay.enabled", havingValue = "true", matchIfMissing = true)
public class AliPayConfig {

    @Bean("alipayClient")
    public AlipayClient alipayClient(AliPayConfigProperties properties) {
        return new DefaultAlipayClient(
                normalizeGatewayUrl(properties.getGatewayUrl()),
                properties.getAppId(),
                properties.getMerchantPrivateKey(),
                properties.getFormat(),
                properties.getCharset(),
                properties.getAlipayPublicKey(),
                properties.getSignType()
        );
    }

    public static String normalizeGatewayUrl(String gatewayUrl) {
        if (gatewayUrl == null) {
            return null;
        }
        String trimmedGatewayUrl = gatewayUrl.trim();
        int firstWhitespace = trimmedGatewayUrl.indexOf(' ');
        return firstWhitespace < 0 ? trimmedGatewayUrl : trimmedGatewayUrl.substring(0, firstWhitespace);
    }
}
