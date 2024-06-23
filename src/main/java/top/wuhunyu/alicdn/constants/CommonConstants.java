package top.wuhunyu.alicdn.constants;

/**
 * 公共常量
 *
 * @author gongzhiqiang
 * @date 2024/06/21 18:56
 **/

public final class CommonConstants {

    private CommonConstants() {
    }

    /**
     * 阿里云配置文件路径
     */
    public static final String CONFIG_PATH = "aliCdnProperties.properties";

    /**
     * 阿里云访问key
     */
    public static final String ACCESS_KEY_ID = "accessKeyId";

    /**
     * 阿里云访问密钥
     */
    public static final String ACCESS_KEY_SECRET = "accessKeySecret";

    /**
     * 阿里云域名
     */
    public static final String DOMAIN = "domain";

    /**
     * 保存 ssl 证书的目录
     */
    public static final String SSL_PATH = "sslPath";

    /**
     * 公钥的名称
     */
    public static final String PUB = "pub";

    /**
     * 私钥的名称
     */
    public static final String PRI = "pri";

    /**
     * 更新的时间表达式
     */
    public static final String SCHEDULED_CRON = "scheduledCron";

    /**
     * 默认的更新的时间表达式
     * 默认每天凌晨 1 点执行
     */
    public static final String DEFAULT_SCHEDULED_CRON = "0 0 1 * * ?";

    /**
     * 文件监听延迟时间，单位：毫秒
     */
    public static final String FILE_MODIFY_LISTEN_DELAY = "fileModifyListenDelay";

    /**
     * 默认文件监听延迟时间，单位：毫秒
     * 默认 1000 毫秒
     */
    public static final long DEFAULT_FILE_MODIFY_LISTEN_DELAY = 1000L;

    /**
     * 当发生异常时，重试的次数
     */
    public static final String RETRY_TIME_WHEN_EXCEPTION = "retryTimeWhenException";

    /**
     * 当发生异常时，重试的次数
     * 默认是 3 次
     */
    public static final int DEFAULT_TIME_RETRY_WHEN_EXCEPTION = 3;

}
