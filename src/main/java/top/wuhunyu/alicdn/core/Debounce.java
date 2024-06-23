package top.wuhunyu.alicdn.core;

import cn.hutool.core.thread.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 防抖器
 * <p>
 * 使用场景：
 * <p>
 * 在连续的指定时间段内，只执行最后一次提交的任务
 * <p>
 * 使用示例
 * <p>
 * {@link Debounce#init} 之后 调用 {@link Debounce#debounce} 实现防抖
 *
 * @author gongzhiqiang
 * @date 2024-06-21 13:51
 */

@Slf4j
public class Debounce implements Closeable {

    private final ScheduledExecutorService scheduledExecutorService;

    private final AtomicReference<Future<?>> task;

    private final Long delay;

    private final TimeUnit unit;

    private Debounce(final String debounceName, final Long delay, final TimeUnit unit) {
        Objects.requireNonNull(debounceName, "防抖器名称不能为空");
        Objects.requireNonNull(delay, "延迟时间不能为空");
        Objects.requireNonNull(unit, "延迟时间单位不能为空");

        scheduledExecutorService = new ScheduledThreadPoolExecutor(
                1,
                new NamedThreadFactory(debounceName + "-", Boolean.TRUE));
        task = new AtomicReference<>();

        this.delay = delay;
        this.unit = unit;
    }

    /**
     * 初始化一个 防抖器
     *
     * @param debounceName 防抖器名称 非空
     * @param delay        延迟时间 非空
     * @param unit         延迟时间单位 非空
     * @return 防抖器实例
     */
    public static Debounce init(final String debounceName, final Long delay, final TimeUnit unit) {
        return new Debounce(debounceName, delay, unit);
    }

    /**
     * 提交需要防抖的任务
     * <p>
     * 保证并发安全
     *
     * @param runnable 需要防抖的事件
     */
    public void debounce(final Runnable runnable) {
        Objects.requireNonNull(delay, "延迟时间不能为空");
        Objects.requireNonNull(unit, "延迟时间单位不能为空");
        Objects.requireNonNull(runnable, "防抖任务不能为空");

        Runnable wrapRunnable = () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error("防抖操作异常: ", e);
            }
        };
        Future<?> curTask = scheduledExecutorService.schedule(wrapRunnable, delay, unit);
        Future<?> preTask = task.get();
        if (task.compareAndSet(preTask, curTask)) {
            this.tryCancel(preTask);
        } else {
            curTask.cancel(Boolean.TRUE);
        }
    }

    /**
     * 尝试取消任务
     *
     * @param task 任务，允许为空
     */
    private void tryCancel(Future<?> task) {
        if (Objects.nonNull(task) && !task.isDone() && !task.isCancelled()) {
            task.cancel(Boolean.TRUE);
        }
    }

    /**
     * 关闭资源
     */
    @Override
    public void close() {
        log.info("正在关闭防抖器");
        this.tryCancel(task.get());
        if (!scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
        }
    }

}
