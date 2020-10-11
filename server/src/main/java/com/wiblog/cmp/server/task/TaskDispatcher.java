//package com.wiblog.cmp.server.task;
//
///**
// * 任务分发器
// */
//public interface TaskDispatcher<ID, T> {
//
//    /**
//     * 提交任务编号，任务，任务过期时间给任务分发器处理
//     * @param id 任务编号
//     * @param task 任务
//     * @param expiryTime 任务过期时间
//     */
//    void process(ID id, T task, long expiryTime);
//
//    void shutdown();
//}
