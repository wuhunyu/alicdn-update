package top.wuhunyu.alicdn.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.aliyun.sdk.service.cdn20180510.models.SetCdnDomainSSLCertificateRequest;
import lombok.extern.slf4j.Slf4j;
import top.wuhunyu.alicdn.properties.AliCdnProperties;
import top.wuhunyu.alicdn.core.MyAliClient;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 修改阿里云 CDN https 证书
 *
 * @author gongzhiqiang
 * @date 2024/06/23 13:56
 **/

@Slf4j
public class SetCdnDomainSSLCertificate {

    private static SetCdnDomainSSLCertificateRequest buildSetCdnDomainSSLCertificateRequest() {
        // 获取 cdn 配置属性
        AliCdnProperties aliCdnProperties = AliCdnProperties.getInstance();

        // 读取公私钥
        String pubPath = aliCdnProperties.getSslPath() + File.separator + aliCdnProperties.getPub();
        String pubStr = "";
        try {
            pubStr = FileUtil.readString(pubPath, StandardCharsets.UTF_8);
        } catch (IORuntimeException e) {
            throw new RuntimeException("读取公钥 " + pubPath + " 异常");
        }

        String priPath = aliCdnProperties.getSslPath() + File.separator + aliCdnProperties.getPri();
        String priStr = "";
        try {
            priStr = FileUtil.readString(priPath, StandardCharsets.UTF_8);
        } catch (IORuntimeException e) {
            throw new RuntimeException("读取私钥 " + priPath + " 异常");
        }

        // 构建 cdn 证书修改请求对象
        return SetCdnDomainSSLCertificateRequest.builder()
                .domainName(aliCdnProperties.getDomain())
                .certName(SetCdnDomainSSLCertificate.generateCertName(aliCdnProperties.getDomain()))
                .certType("upload")
                .SSLProtocol("on")
                .SSLPub(pubStr)
                .SSLPri(priStr)
                .build();
    }

    private static String generateCertName(String domain) {
        return domain + "-" + System.currentTimeMillis();
    }

    public static void invoke() {
        // 获取 cdn 配置属性
        int retryTimeWhenException = AliCdnProperties.getInstance()
                .getRetryTimeWhenException();

        for (int i = 0; i < retryTimeWhenException; i++) {
            try {
                // 构建请求对象
                SetCdnDomainSSLCertificateRequest setCdnDomainSSLCertificateRequest =
                        SetCdnDomainSSLCertificate.buildSetCdnDomainSSLCertificateRequest();
                // 执行修改
                MyAliClient.INSTANCE.getAliClient().setCdnDomainSSLCertificate(setCdnDomainSSLCertificateRequest);
                log.info("修改阿里云 CDN https 证书成功");
                return;
            } catch (Exception e) {
                log.warn("第 {} 次尝试失败：", i + 1, e);
            }
        }
    }

}
