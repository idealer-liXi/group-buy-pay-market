package cn.idealer01.infrastructure.oss;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oss.aliyun")
public class AliyunOssProperties {
    private String endpoint;
    private String bucketName;
    private String accessKeyId;
    private String accessKeySecret;
    private String dirPrefix = "goods/images";
    private String publicBaseUrl;

    public boolean isConfigured() {
        return StringUtils.isNoneBlank(endpoint, bucketName, accessKeyId, accessKeySecret, publicBaseUrl);
    }
}
