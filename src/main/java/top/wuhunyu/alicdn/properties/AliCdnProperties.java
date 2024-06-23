package top.wuhunyu.alicdn.properties;

import cn.hutool.cron.CronException;
import cn.hutool.cron.pattern.parser.PatternParser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

import static top.wuhunyu.alicdn.constants.CommonConstants.*;

/**
 * 阿里云CDN属性
 *
 * @author gongzhiqiang
 * @date 2024/06/21 19:10
 **/

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AliCdnProperties {

    private static volatile AliCdnProperties instance;

    private String accessKeyId;

    private String accessKeySecret;

    private String domain;

    private String sslPath;

    private String pub;

    private String pri;

    private String scheduledCron;

    private Long fileModifyListenDelay;

    private Integer retryTimeWhenException;

    public static AliCdnProperties getInstance() {
        if (Objects.nonNull(instance)) {
            return instance;
        }
        synchronized (AliCdnProperties.class) {
            if (Objects.nonNull(instance)) {
                return instance;
            }
            instance = AliCdnProperties.init();
        }
        return instance;
    }

    private static AliCdnProperties init() {
        // 读取配置文件
        Properties aliCdnFromProperties = AliCdnProperties.readFromProperties();

        String accessKeyId = AliCdnProperties.readDefault(aliCdnFromProperties, ACCESS_KEY_ID);
        String accessKeySecret = AliCdnProperties.readDefault(aliCdnFromProperties, ACCESS_KEY_SECRET);
        String domain = AliCdnProperties.readDefault(aliCdnFromProperties, DOMAIN);
        String sslPath = AliCdnProperties.readDefault(aliCdnFromProperties, SSL_PATH);
        String pub = AliCdnProperties.readDefault(aliCdnFromProperties, PUB);
        String pri = AliCdnProperties.readDefault(aliCdnFromProperties, PRI);
        String scheduledCron = AliCdnProperties.readDefault(aliCdnFromProperties, SCHEDULED_CRON);
        String fileModifyListenDelayStr =
                AliCdnProperties.readDefault(aliCdnFromProperties, FILE_MODIFY_LISTEN_DELAY);
        String retryTimeWhenExceptionStr =
                AliCdnProperties.readDefault(aliCdnFromProperties, RETRY_TIME_WHEN_EXCEPTION);

        Objects.requireNonNull(accessKeyId, "阿里云访问key不能为空");
        Objects.requireNonNull(accessKeySecret, "阿里云访问密钥不能为空");
        Objects.requireNonNull(domain, "阿里云域名不能为空");
        Objects.requireNonNull(sslPath, "保存 ssl 证书的目录不能为空");
        Objects.requireNonNull(pub, "公钥的名称不能为空");
        Objects.requireNonNull(pri, "私钥的名称不能为空");

        // 证书存在性验证
        String pubPath = sslPath + File.separatorChar + pub;
        if (!new File(pubPath).isFile()) {
            throw new IllegalArgumentException("公钥 " + pubPath + "不是一个有效的文件");
        }
        String priPath = sslPath + File.separatorChar + pub;
        if (!new File(priPath).isFile()) {
            throw new IllegalArgumentException("私钥 " + priPath + "不是一个有效的文件");
        }

        // cron 表达式验证
        if (StringUtils.isBlank(scheduledCron)) {
            scheduledCron = DEFAULT_SCHEDULED_CRON;
        } else {
            // 校验 cron 表达式的有效性
            try {
                PatternParser.parse(scheduledCron);
            } catch (CronException e) {
                throw new IllegalArgumentException("更新的时间表达式 " + scheduledCron + " 格式错误");
            }
        }

        // 文件监听延迟时间
        long fileModifyListenDelay = DEFAULT_FILE_MODIFY_LISTEN_DELAY;
        if (StringUtils.isNotBlank(fileModifyListenDelayStr)) {
            try {
                fileModifyListenDelay = Long.parseLong(fileModifyListenDelayStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("文件监听延迟时间（单位：毫秒） " + fileModifyListenDelayStr +
                        " 不是一个数字");
            }
        }

        // 异常时重试的次数
        int retryTimeWhenException = DEFAULT_TIME_RETRY_WHEN_EXCEPTION;
        if (StringUtils.isNotBlank(retryTimeWhenExceptionStr)) {
            try {
                retryTimeWhenException = Integer.parseInt(retryTimeWhenExceptionStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("异常时重试的次数 " + retryTimeWhenExceptionStr + " 不是一个数字");
            }
        }

        // 返回构建的配置类
        AliCdnProperties aliCdnProperties = AliCdnProperties.builder()
                .accessKeyId(accessKeyId)
                .accessKeySecret(accessKeySecret)
                .domain(domain)
                .sslPath(sslPath)
                .pub(pub)
                .pri(pri)
                .scheduledCron(scheduledCron)
                .fileModifyListenDelay(fileModifyListenDelay)
                .retryTimeWhenException(retryTimeWhenException)
                .build();

        // 打印
        AliCdnProperties.print(aliCdnProperties);

        return aliCdnProperties;
    }

    private static Properties readFromProperties() {
        Properties properties = new Properties();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        AliCdnProperties.class
                                .getClassLoader()
                                .getResourceAsStream(CONFIG_PATH), StandardCharsets.UTF_8))) {
            properties.load(reader);
        } catch (Exception e) {
            log.warn("读取阿里云配置文件失败：", e);
        }
        return properties;
    }

    private static String readDefault(Properties aliCdnFromProperties, String key) {
        String value = System.getenv(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return StringUtils.isBlank(aliCdnFromProperties.getProperty(key)) ?
                null :
                aliCdnFromProperties.getProperty(key);
    }

    private static void print(AliCdnProperties aliCdnProperties) {
        log.info("读取配置信息如下：");
        log.info("阿里云访问key：{}", aliCdnProperties.getAccessKeyId());
        log.info("阿里云域名：{}", aliCdnProperties.getDomain());
        log.info("保存 ssl 证书的目录：{}", aliCdnProperties.getSslPath());
        log.info("公钥的名称：{}", aliCdnProperties.getPub());
        log.info("私钥的名称：{}", aliCdnProperties.getPri());
        log.info("更新的时间表达式：{}", aliCdnProperties.getScheduledCron());
        log.info("文件监听延迟时间：{}", aliCdnProperties.getFileModifyListenDelay());
        log.info("异常时重试的次数：{}", aliCdnProperties.getRetryTimeWhenException());
    }

}
