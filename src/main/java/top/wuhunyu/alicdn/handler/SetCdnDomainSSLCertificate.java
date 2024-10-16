package top.wuhunyu.alicdn.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.aliyun.apache.hc.core5.http.HttpStatus;
import com.aliyun.sdk.service.cdn20180510.models.SetCdnDomainSSLCertificateRequest;
import com.aliyun.sdk.service.cdn20180510.models.SetCdnDomainSSLCertificateResponse;
import com.aliyun.sdk.service.cdn20180510.models.SetCdnDomainSSLCertificateResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.wuhunyu.alicdn.core.MyAliClient;
import top.wuhunyu.alicdn.properties.AliCdnProperties;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 修改阿里云 CDN https 证书
 *
 * @author gongzhiqiang
 * @date 2024/06/23 13:56
 **/

@Slf4j
public class SetCdnDomainSSLCertificate {

    private static List<SetCdnDomainSSLCertificateRequest> buildSetCdnDomainSSLCertificateRequests(String domain) {
        // 获取 cdn 配置属性
        AliCdnProperties aliCdnProperties = AliCdnProperties.getInstance();

        // 是否存在于需要更新的域名列表
        List<String> domains = aliCdnProperties.getDomains();
        List<String> pubes = aliCdnProperties.getPubes();
        List<String> pries = aliCdnProperties.getPries();

        // 需要更新的 域名，公钥，私钥
        List<ImmutableTriple<String, String, String>> triples = new ArrayList<>();
        int n = domains.size();
        for (int i = 0; i < n; i++) {
            String curDomain = domains.get(i);
            String curPub = pubes.get(i);
            String curPri = pries.get(i);
            if (StringUtils.isBlank(domain) || Objects.equals(domain, curDomain)) {
                triples.add(ImmutableTriple.of(curDomain, curPub, curPri));
            }
        }

        // 请求对象容器
        List<SetCdnDomainSSLCertificateRequest> ans = new ArrayList<>(triples.size());

        for (ImmutableTriple<String, String, String> triple : triples) {
            String curDomain = triple.getLeft();
            String curPub = triple.getMiddle();
            String curPri = triple.getRight();

            // 读取公私钥
            String pubPath = aliCdnProperties.getSslPath() + File.separator + curDomain + File.separator + curPub;
            String pubStr = "";
            try {
                pubStr = FileUtil.readString(pubPath, StandardCharsets.UTF_8);
            } catch (IORuntimeException e) {
                throw new RuntimeException("读取公钥 " + pubPath + " 异常");
            }

            String priPath = aliCdnProperties.getSslPath() + File.separator + curDomain + File.separator + curPri;
            String priStr = "";
            try {
                priStr = FileUtil.readString(priPath, StandardCharsets.UTF_8);
            } catch (IORuntimeException e) {
                throw new RuntimeException("读取私钥 " + priPath + " 异常");
            }

            // 构建 cdn 证书修改请求对象
            ans.add(SetCdnDomainSSLCertificateRequest.builder()
                    .domainName(curDomain)
                    .certName(SetCdnDomainSSLCertificate.generateCertName(curDomain))
                    .certType("upload")
                    .SSLProtocol("on")
                    .SSLPub(pubStr)
                    .SSLPri(priStr)
                    .build());
        }

        return ans;
    }

    private static String generateCertName(String domain) {
        return domain + "-" + System.currentTimeMillis();
    }

    public static void invoke(String domain) {
        // 构建请求对象
        for (SetCdnDomainSSLCertificateRequest setCdnDomainSSLCertificateRequest :
                SetCdnDomainSSLCertificate.buildSetCdnDomainSSLCertificateRequests(domain)) {
            final String curDomain = setCdnDomainSSLCertificateRequest.getDomainName();
            // 执行修改
            MyAliClient.INSTANCE.getAliClient()
                    .setCdnDomainSSLCertificate(setCdnDomainSSLCertificateRequest)
                    .thenAccept(setCdnDomainSSLCertificateResponse -> {
                        // 相应状态码
                        Integer statusCode = setCdnDomainSSLCertificateResponse.getStatusCode();
                        if (Objects.equals(statusCode, HttpStatus.SC_OK)) {
                            log.info("<= 修改阿里云 {} CDN https 证书成功", curDomain);
                            return;
                        }
                        // 请求id
                        String requestId = Optional.of(setCdnDomainSSLCertificateResponse)
                                .map(SetCdnDomainSSLCertificateResponse::getBody)
                                .map(SetCdnDomainSSLCertificateResponseBody::getRequestId)
                                .orElse("");
                        log.warn("<= 修改阿里云 {} CDN https 证书失败，状态码：{}，请求id：{}",
                                curDomain, statusCode, requestId);
                    }).exceptionally(e -> {
                        log.warn("<= 修改阿里云 {} CDN https 证书失败，异常堆栈信息：", curDomain, e);
                        return null;
                    });
            log.info("=> 修改阿里云 {} CDN https 证书请求发起成功", curDomain);
        }
    }

}
