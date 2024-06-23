package top.wuhunyu.alicdn.core;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.ICredentialProvider;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.cdn20180510.AsyncClient;
import com.aliyun.sdk.service.cdn20180510.DefaultAsyncClientBuilder;
import top.wuhunyu.alicdn.properties.AliCdnProperties;

/**
 * 自定义阿里云客户端
 *
 * @author gongzhiqiang
 * @date 2024/06/23 13:29
 **/

public enum MyAliClient {

    INSTANCE;

    private final AsyncClient aliClient;

    MyAliClient() {
        AliCdnProperties aliCdnProperties = AliCdnProperties.getInstance();
        // 阿里云访问key
        String accessKeyId = aliCdnProperties.getAccessKeyId();
        // 阿里云访问密钥
        String accessKeySecret = aliCdnProperties.getAccessKeySecret();

        // 创建一个阿里云客户端
        Credential credential = Credential.builder()
                .accessKeyId(accessKeyId)
                .accessKeySecret(accessKeySecret)
                .build();
        try (ICredentialProvider credentialProvider = StaticCredentialProvider.create(credential)) {
            DefaultAsyncClientBuilder defaultAsyncClientBuilder = new DefaultAsyncClientBuilder();
            defaultAsyncClientBuilder.credentialsProvider(credentialProvider);
            aliClient = defaultAsyncClientBuilder.build();
        }
    }


    public AsyncClient getAliClient() {
        return aliClient;
    }
}
