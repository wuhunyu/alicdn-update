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

    public static void schedule() {
        AliCdnProperties instance = AliCdnProperties.getInstance();
        CronUtil.schedule(instance.getScheduledCron(), (Runnable) SetCdnDomainSSLCertificate::invoke);
    }

    public static void listen() {
        ListenFileModify.listen(SetCdnDomainSSLCertificate::invoke);
    }

    public static void main(String[] args) throws InterruptedException {
        // 启动应用
        log.info("阿里云cdn更新程序启动中");
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