package top.wuhunyu.alicdn;

import cn.hutool.cron.CronUtil;
import lombok.extern.slf4j.Slf4j;
import top.wuhunyu.alicdn.handler.ListenFileModify;
import top.wuhunyu.alicdn.handler.SetCdnDomainSSLCertificate;
import top.wuhunyu.alicdn.properties.AliCdnProperties;

/**
 * 阿里云cdn更新
 *
 * @author gongzhiqiang
 * @date 2024/06/21 18:52
 **/

@Slf4j
public class AliCdnUpdateApplication {

    /**
     * 启动一个定时器，用于周期性执行阿里云 cdn 证书更新任务
     */
    private static void schedule() {
        AliCdnProperties instance = AliCdnProperties.getInstance();
        CronUtil.schedule(instance.getScheduledCron(), (Runnable) () -> {
            log.info("触发定时器，执行阿里云 CDN 证书更新任务");
            SetCdnDomainSSLCertificate.invoke();
        });
        // 设置支持秒级任务
        CronUtil.setMatchSecond(Boolean.TRUE);
        // 启动定时器
        CronUtil.start(Boolean.TRUE);
    }

    /**
     * 启动一个监听器，用于监听证书文件的变化，并触发阿里云 cdn 证书更新任务
     */
    private static void listen() {
        ListenFileModify.listen(SetCdnDomainSSLCertificate::invoke);
    }

    public static void main(String[] args) throws InterruptedException {
        // 启动应用
        log.info("阿里云 CDN 更新程序启动中");
        // 启动定时任务
        schedule();
        // 启动证书监听器
        listen();
        // 主动执行一次证书修改
        SetCdnDomainSSLCertificate.invoke();
        // 阻塞主线程
        synchronized (AliCdnUpdateApplication.class) {
            AliCdnUpdateApplication.class.wait();
        }
    }

}