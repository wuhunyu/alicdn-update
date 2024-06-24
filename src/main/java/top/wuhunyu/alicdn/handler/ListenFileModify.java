package top.wuhunyu.alicdn.handler;

import cn.hutool.core.io.watch.SimpleWatcher;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.watchers.DelayWatcher;
import lombok.extern.slf4j.Slf4j;
import top.wuhunyu.alicdn.properties.AliCdnProperties;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Objects;

/**
 * 监听文件修改事件
 *
 * @author gongzhiqiang
 * @date 2024/06/23 16:07
 **/

@Slf4j
public class ListenFileModify {

    public static void listen(final Runnable work) {
        AliCdnProperties aliCdnProperties = AliCdnProperties.getInstance();
        WatchMonitor.createAll(aliCdnProperties.getSslPath(), new DelayWatcher(new SimpleWatcher() {
            @Override
            public void onModify(WatchEvent<?> event, Path currentPath) {
                Path filePath = (Path) event.context();
                String fileName = filePath.toFile()
                        .getName();
                // 但公钥或者私钥发生变化时
                if (Objects.equals(fileName, aliCdnProperties.getPub()) ||
                        Objects.equals(fileName, aliCdnProperties.getPri())) {
                    log.info("监听到 {} 被修改，尝试执行更新任务", fileName);
                    // 主动更新一次
                    work.run();
                }
            }
        }, aliCdnProperties.getFileModifyListenDelay())).start();
    }

}
