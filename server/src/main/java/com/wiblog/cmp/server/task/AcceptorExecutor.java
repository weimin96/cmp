//package com.wiblog.cmp.server.task;
//
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//
///**
// * 任务接收执行器
// */
//public class AcceptorExecutor<ID, T> {
//
//    /**
//     * 执行队列最大数量
//     */
//    private final int maxBufferSize;
//
//    /**
//     * 单个批量任务包含任务最大数量
//     */
//    private final int maxBatchingSize;
//
//    /**
//    * 批量任务等待最大延迟时长，单位：毫秒
//    */
//    private final long maxBatchingDelay;
//
//    /**
//     * 是否关闭
//     */
//    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
//
//    /**
//     * 批量任务处理信号量
//     */
//    private final Semaphore batchWorkRequests = new Semaphore(0);
//
//    /**
//     * 接收任务队列
//     */
//    private final BlockingQueue<TaskHolder<ID, T>> acceptorQueue = new LinkedBlockingQueue<>();
//
//    /**
//     * 重新执行任务队列
//     */
//    private final BlockingDeque<TaskHolder<ID, T>> reprocessQueue = new LinkedBlockingDeque<>();
//
//    /**
//     * 待执行任务映射
//     */
//    private final Map<ID, TaskHolder<ID, T>> pendingTasksMap = new HashMap<>();
//
//    /**
//     * 接收任务线程
//     */
//    private final Thread acceptorThread;
//
//    /**
//     * 待执行队列
//     */
//    private final Deque<ID> processingOrder = new LinkedList<>();
//
//    /**
//     * 批量任务工作队列
//     */
//    private final BlockingQueue<List<TaskHolder<ID, T>>> batchWorkQueue = new LinkedBlockingQueue<>();
//
//    /**
//     * 被抛弃的待处理任务数量（用于监控）
//     */
//    volatile long queueOverflows;
//
//    /**
//     * 被覆盖的待处理任务数量 （用于监控）
//     */
//    volatile long overriddenTasks;
//
//    /**
//     * 过期的任务数 （用于监控）
//     */
//    volatile long expiredTasks;
//
//    AcceptorExecutor(String id,
//                     int maxBufferSize,
//                     int maxBatchingSize,
//                     long maxBatchingDelay,
//                     long congestionRetryDelayMs,
//                     long networkFailureRetryMs) {
//        this.maxBufferSize = maxBufferSize;
//        this.maxBatchingSize = maxBatchingSize;
//        this.maxBatchingDelay = maxBatchingDelay;
//
//        // // 创建 接收任务线程
//        ThreadGroup threadGroup = new ThreadGroup("taskExecutors");
//        this.acceptorThread = new Thread(threadGroup, new AcceptorRunner(), "TaskAcceptor-" + id);
//        this.acceptorThread.setDaemon(true);
//        this.acceptorThread.start();
//    }
//
//    /**
//     * 结束任务
//     */
//    void shutdown() {
//        // 原子操作 只允许关闭一次
//        if (isShutdown.compareAndSet(false, true)) {
//            // 中断线程
//            acceptorThread.interrupt();
//        }
//    }
//
//    class AcceptorRunner implements Runnable {
//        @Override
//        public void run() {
//            long scheduleTime = 0;
//            // 无限循环执行调度，直到关闭
//            while (!isShutdown.get()) {
//                try {
//                    // 循环处理完输入队列
//                    drainInputQueues();
//
//                    int totalItems = processingOrder.size();
//
//                    long now = System.currentTimeMillis();
//                    if (scheduleTime < now) {
//                        scheduleTime = now + trafficShaper.transmissionDelay();
//                    }
//                    if (scheduleTime <= now) {
//                        assignBatchWork();
//                        assignSingleItemWork();
//                    }
//
//                    // If no worker is requesting data or there is a delay injected by the traffic shaper,
//                    // sleep for some time to avoid tight loop.
//                    if (totalItems == processingOrder.size()) {
//                        Thread.sleep(10);
//                    }
//                } catch (InterruptedException ex) {
//                    // Ignore
//                } catch (Throwable e) {
//                    // Safe-guard, so we never exit this loop in an uncontrolled way.
//                }
//            }
//        }
//
//        /**
//         * 待执行任务已满
//         */
//        private boolean isFull() {
//            return pendingTasksMap.size() >= maxBufferSize;
//        }
//
//        /**
//         * 处理完输入队列( 接收队列 + 重新执行队列 )
//         * @throws InterruptedException
//         */
//        private void drainInputQueues() throws InterruptedException {
//            do {
//                // 处理重新执行队列 提交到执行队列和待执行map中
//                drainReprocessQueue();
//                // 处理接收队列 提交到执行队列和待执行map中
//                drainAcceptorQueue();
//
//                if (!isShutdown.get()) {
//                    // 所有队列为空，等待 10 ms，看接收队列是否有新任务
//                    if (reprocessQueue.isEmpty() && acceptorQueue.isEmpty() && pendingTasksMap.isEmpty()) {
//                        TaskHolder<ID, T> taskHolder = acceptorQueue.poll(10, TimeUnit.MILLISECONDS);
//                        if (taskHolder != null) {
//                            // 将接收的新任务放入代处理队列
//                            appendTaskHolder(taskHolder);
//                        }
//                    }
//                }
//            } while (!reprocessQueue.isEmpty() || !acceptorQueue.isEmpty() || pendingTasksMap.isEmpty());
//        }
//
//        /**
//         * 处理接收队列
//         */
//        private void drainAcceptorQueue() {
//            while (!acceptorQueue.isEmpty()) {
//                appendTaskHolder(acceptorQueue.poll());
//            }
//        }
//
//        /**
//         * 处理重新执行队列
//         */
//        private void drainReprocessQueue() {
//            long now = System.currentTimeMillis();
//            while (!reprocessQueue.isEmpty() && !isFull()) {
//                // 优先拿较新的任务
//                TaskHolder<ID, T> taskHolder = reprocessQueue.pollLast();
//                ID id = taskHolder.getId();
//                // 过期
//                if (taskHolder.getExpiryTime() <= now) {
//                    expiredTasks++;
//                } else if (pendingTasksMap.containsKey(id)) { // 已存在
//                    overriddenTasks++;
//                } else {
//                    pendingTasksMap.put(id, taskHolder);
//                    // 提交到执行队列队头
//                    processingOrder.addFirst(id);
//                }
//            }
//            // 如果待执行队列已满，清空重新执行队列，放弃较早的任务
//            if (isFull()) {
//                queueOverflows += reprocessQueue.size();
//                reprocessQueue.clear();
//            }
//        }
//
//        /**
//         * 提交到执行队列和待执行map中
//         */
//        private void appendTaskHolder(TaskHolder<ID, T> taskHolder) {
//            // 如果待执行队列已满，移除待处理队列，放弃较早的任务
//            if (isFull()) {
//                pendingTasksMap.remove(processingOrder.poll());
//                queueOverflows++;
//            }
//            // 添加到待执行队列
//            TaskHolder<ID, T> previousTask = pendingTasksMap.put(taskHolder.getId(), taskHolder);
//            // 没有被添加过则放入执行队列
//            if (previousTask == null) {
//                processingOrder.add(taskHolder.getId());
//            } else {
//                // 任务被覆盖
//                overriddenTasks++;
//            }
//        }
//
//        void assignSingleItemWork() {
//            if (!processingOrder.isEmpty()) {
//                if (singleItemWorkRequests.tryAcquire(1)) {
//                    long now = System.currentTimeMillis();
//                    while (!processingOrder.isEmpty()) {
//                        ID id = processingOrder.poll();
//                        TaskHolder<ID, T> holder = pendingTasksMap.remove(id);
//                        if (holder.getExpiryTime() > now) {
//                            singleItemWorkQueue.add(holder);
//                            return;
//                        }
//                        expiredTasks++;
//                    }
//                    singleItemWorkRequests.release();
//                }
//            }
//        }
//
//        /**
//         * 调度批量任务
//         */
//        void assignBatchWork() {
//            // 满足批量任务调度条件
//            if (hasEnoughTasksForNextBatch()) {
//                // 获取 批量任务工作请求信号量 (无阻塞)
//                if (batchWorkRequests.tryAcquire(1)) {
//                    long now = System.currentTimeMillis();
//                    int len = Math.min(maxBatchingSize, processingOrder.size());
//                    List<TaskHolder<ID, T>> holders = new ArrayList<>(len);
//                    // 获取批量任务
//                    while (holders.size() < len && !processingOrder.isEmpty()) {
//                        ID id = processingOrder.poll();
//                        TaskHolder<ID, T> holder = pendingTasksMap.remove(id);
//                        if (holder.getExpiryTime() > now) {
//                            holders.add(holder);
//                        } else {
//                            expiredTasks++;
//                        }
//                    }
//                    // 未调度到批量任务，释放请求信号量
//                    if (holders.isEmpty()) {
//                        batchWorkRequests.release();
//                    } else {
//                        // 添加批量任务到批量任务工作队列
//                        batchSizeMetric.record(holders.size(), TimeUnit.MILLISECONDS);
//                        batchWorkQueue.add(holders);
//                    }
//                }
//            }
//        }
//
//        /**
//         * 判断是否有足够任务进行下一次批量任务调度
//         */
//        private boolean hasEnoughTasksForNextBatch() {
//            if (processingOrder.isEmpty()) {
//                return false;
//            }
//            // 待执行任务映射已满
//            if (pendingTasksMap.size() >= maxBufferSize) {
//                return true;
//            }
//
//            // 到达批量任务处理最大等待延迟( 通过待处理队列的头部任务判断 )
//            TaskHolder<ID, T> nextHolder = pendingTasksMap.get(processingOrder.peek());
//            long delay = System.currentTimeMillis() - nextHolder.getSubmitTimestamp();
//            // 最大时间是否超过最大等待延迟
//            return delay >= maxBatchingDelay;
//        }
//    }
//}
