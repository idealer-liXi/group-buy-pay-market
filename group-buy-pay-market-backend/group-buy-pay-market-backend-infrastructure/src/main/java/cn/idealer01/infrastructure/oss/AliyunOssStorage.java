package cn.idealer01.infrastructure.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class AliyunOssStorage {
    private final AliyunOssProperties properties;

    public AliyunOssStorage(AliyunOssProperties properties) {
        this.properties = properties;
    }

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    public String getDirPrefix() {
        return StringUtils.removeEnd(properties.getDirPrefix(), "/");
    }

    public String buildPublicUrl(String objectKey) {
        String baseUrl = StringUtils.removeEnd(properties.getPublicBaseUrl(), "/");
        return baseUrl + "/" + objectKey;
    }

    public void upload(String objectKey, InputStream inputStream, long size, String contentType) {
        OSS oss = new OSSClientBuilder().build(properties.getEndpoint(), properties.getAccessKeyId(), properties.getAccessKeySecret());
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(size);
            metadata.setContentType(contentType);
            oss.putObject(properties.getBucketName(), objectKey, inputStream, metadata);
        } finally {
            oss.shutdown();
        }
    }

    public void delete(String objectKey) {
        OSS oss = new OSSClientBuilder().build(properties.getEndpoint(), properties.getAccessKeyId(), properties.getAccessKeySecret());
        try {
            oss.deleteObject(properties.getBucketName(), objectKey);
        } finally {
            oss.shutdown();
        }
    }
}
