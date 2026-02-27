package com.openclaw.utils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SchedulerUtils {

    private static SchedulerUtils instance;
    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> tasks;
    private final AtomicInteger taskIdGenerator;

    private SchedulerUtils() {
        this.scheduler = Executors.newScheduledThreadPool(5);
        this.tasks = new ConcurrentHashMap<>();
        this.taskIdGenerator = new AtomicInteger(1);
    }

    public static synchronized SchedulerUtils getInstance() {
        if (instance == null) {
            instance = new SchedulerUtils();
        }
        return instance;
    }

    public String scheduleTask(Runnable task, long delay, TimeUnit unit) {
        String taskId = "task_" + taskIdGenerator.incrementAndGet();
        ScheduledFuture<?> future = scheduler.schedule(task, delay, unit);
        tasks.put(taskId, future);
        return taskId;
    }

    public String scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        String taskId = "task_" + taskIdGenerator.incrementAndGet();
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
        tasks.put(taskId, future);
        return taskId;
    }

    public String scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        String taskId = "task_" + taskIdGenerator.incrementAndGet();
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(task, initialDelay, delay, unit);
        tasks.put(taskId, future);
        return taskId;
    }

    public boolean cancelTask(String taskId) {
        ScheduledFuture<?> future = tasks.remove(taskId);
        if (future != null) {
            return future.cancel(false);
        }
        return false;
    }

    public List<String> listTasks() {
        return new ArrayList<>(tasks.keySet());
    }

    public boolean isTaskRunning(String taskId) {
        ScheduledFuture<?> future = tasks.get(taskId);
        return future != null && !future.isDone();
    }

    public void shutdown() {
        scheduler.shutdown();
        tasks.clear();
    }

    public static void main(String[] args) {
        SchedulerUtils scheduler = SchedulerUtils.getInstance();

        // 测试任务
        String task1 = scheduler.scheduleAtFixedRate(() -> {
            System.out.println("定时任务执行: " + new Date());
        }, 0, 2, TimeUnit.SECONDS);

        System.out.println("任务ID: " + task1);
        System.out.println("任务列表: " + scheduler.listTasks());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("取消任务: " + scheduler.cancelTask(task1));
        System.out.println("任务列表: " + scheduler.listTasks());

        scheduler.shutdown();
    }
}
