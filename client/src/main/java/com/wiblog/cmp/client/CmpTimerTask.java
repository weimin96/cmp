package com.wiblog.cmp.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 监管定时任务
 * 固定间隔的周期性任务，一旦遇到超时就会将下一个周期的间隔时间调大，
 * 如果连续超时，那么每次间隔时间都会增大一倍，一直到达外部参数设定的上限为止，
 * 一旦新任务不再超时，间隔时间又会自动恢复为初始值
 *
 * @author pwm
 * @date 2020/2/10
 */
public class CmpTimerTask extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(CmpTimerTask.class);

    /**
     * 定时任务服务
     */
    private final ScheduledExecutorService scheduler;
    /**
     * 子任务线程池
     */
    private final ThreadPoolExecutor executor;
    /**
     * 子任务执行超时时间
     */
    private final long timeoutMillis;
    /**
     * 当前任子务执行频率
     */
    private final AtomicLong delay;
    /**
     * 最大子任务执行频率
     */
    private final long maxDelay;
    /**
     * 子任务
     */
    private final Runnable task;

    public CmpTimerTask(String name, ScheduledExecutorService scheduler, ThreadPoolExecutor executor,
                        int timeout, TimeUnit timeUnit, int expBackOffBound, Runnable task) {
        this.executor = executor;
        this.scheduler = scheduler;
        this.timeoutMillis = timeUnit.toMillis(timeout);
        this.delay = new AtomicLong(timeoutMillis);
        this.maxDelay = timeout * expBackOffBound;
        this.task = task;
    }

    @Override
    public void run() {
        Future<?> future = null;
        //  提交任务
        try {
            future = executor.submit(task);
            // 阻塞 等待任务执行完成或超时
            future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            // 设置下次任务执行频率
            delay.set(timeoutMillis);
        } catch (TimeoutException e) {
            logger.warn("监管任务超时", e);
            // 设置下次任务执行频率
            long currentDelay = delay.get();
            // 如果多次超时，超时时间不断乘以 2 ，不允许超过最大延迟时间 maxDelay
            long newDelay = Math.min(maxDelay, currentDelay * 2);
            // 交换两值 cas操作 多线程
            delay.compareAndSet(currentDelay, newDelay);
        } catch (RejectedExecutionException e) {
            //一旦线程池的阻塞队列中放满了待处理任务，触发了拒绝策略，就会将调度器停掉
            if (executor.isShutdown() || scheduler.isShutdown()) {
                logger.warn("监管任务已经关闭", e);
            } else {
                logger.warn("监管任务拒绝接受任务", e);
            }
        } catch (Throwable e) {
            if (executor.isShutdown() || scheduler.isShutdown()) {
                logger.warn("监管任务已经关闭");
            } else {
                logger.warn("监管任务异常", e);
            }
        } finally {
            if (future != null) {
                future.cancel(true);
            }
            // 再次执行
            if (!scheduler.isShutdown()) {
                scheduler.schedule(this, delay.get(), TimeUnit.MILLISECONDS);
            }
        }
    }
}
