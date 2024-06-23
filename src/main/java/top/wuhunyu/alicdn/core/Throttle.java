package top.wuhunyu.alicdn.core;

import cn.hutool.core.thread.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 节流器
 * <p>
 * 使用场景：
 * <p>
 * 在连续的指定时间段内，只执行最后一次提交的任务
 * <p>
 * 使用示例
 * <p>
 * {@link Throttle#init} 之后 调用 {@link Throttle#throttle} 实现节流
 *
 * @author gongzhiqiang
 * @date 2024-06-21 15:43
 */

@Slf4j
public class Throttle implements Closeable {

    /**
     * 是否正在运行
     * <p>
     * true：正在运行中；false：未运行
     */
    private final AtomicBoolean isRunning;

    private final long delay;

    private final ExecutorService executorService;

    private volatile long lastTime;

    private Throttle(final String throttleName, final Long delay, final TimeUnit unit) {
        Objects.requireNonNull(throttleName, "节流器名称不能为空");
        Objects.requireNonNull(delay, "延迟时间不能为空");
        Objects.requireNonNull(unit, "延迟时间单位不能为空");

        this.lastTime = -1L;
        this.isRunning = new AtomicBoolean(Boolean.FALSE);
        this.delay = unit.toMillis(delay);
        this.executorService = new ThreadPoolExecutor(
                1,
                1,
                120,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                new NamedThreadFactory(throttleName + "-", Boolean.TRUE),
                new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 初始化一个 节流器
     *
     * @param throttleName 节流器名称 非空
     * @param delay        延迟时间 非空
     * @param unit         延迟时间单位 非空
     * @return 节流器实例
     */
    public static Throttle init(final String throttleName, final Long delay, final TimeUnit unit) {
        return new Throttle(throttleName, delay, unit);
    }

    /**
     * 提交需要节流的任务
     * <p>
     * 保证并发安全
     *
     * @param runnable 需要节流的事件 非空
     */
    public void throttle(final Runnable runnable) {
        Objects.requireNonNull(runnable, "节流任务不能为空");

        Runnable wrapRunnable = () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error("节流操作异常: ", e);
            }
            isRunning.set(Boolean.FALSE);
        };

        long now = System.currentTimeMillis();
        if (lastTime < now && isRunning.compareAndSet(Boolean.FALSE, Boolean.TRUE)) {
            lastTime = now + delay;
            executorService.submit(wrapRunnable);
        }
    }

    @Override
    public void close() {
        log.info("正在关闭节流器");
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
