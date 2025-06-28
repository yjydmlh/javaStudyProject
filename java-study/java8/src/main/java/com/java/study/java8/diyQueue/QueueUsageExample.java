package com.java.study.java8.diyQueue;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * ModernHighPerformanceMPMCQueue使用示例
 * 展示各种使用场景和最佳实践
 */
public class QueueUsageExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== ModernHighPerformanceMPMCQueue 使用示例 ===\n");

        // 1. 基本使用
        basicUsageExample();

        // 2. 多线程生产者消费者模式
        producerConsumerExample();

        // 3. 批处理示例
        batchOperationsExample();

        // 4. 阻塞操作示例
        blockingOperationsExample();

        // 5. 队列监控示例
        monitoringExample();
    }

    /**
     * 1. 基本使用示例
     */
    private static void basicUsageExample() {
        System.out.println("=== 1. 基本使用示例 ===");

        // 创建队列 - 三种方式

        // 方式1: 使用默认构造函数
        ModernHighPerformanceMPMCQueue<String> queue1 = new ModernHighPerformanceMPMCQueue<>();

        // 方式2: 指定容量
        ModernHighPerformanceMPMCQueue<String> queue2 = new ModernHighPerformanceMPMCQueue<>(1024);

        // 方式3: 使用构建器
        ModernHighPerformanceMPMCQueue<String> queue = QueueBuilder.<String>newBuilder()
                .capacity(1024)
                .build();

        // 基本操作
        System.out.println("基本入队出队操作:");

        // 入队
        boolean success = queue.offer("Hello");
        System.out.println("入队 'Hello': " + success);

        queue.offer("World");
        queue.offer("Java");

        System.out.println("当前队列大小: " + queue.size());

        // 出队
        String item1 = queue.poll();
        String item2 = queue.poll();
        String item3 = queue.poll();
        String item4 = queue.poll(); // 应该返回null

        System.out.println("出队结果: " + item1 + ", " + item2 + ", " + item3 + ", " + item4);
        System.out.println("队列是否为空: " + queue.isEmpty());
        System.out.println();
    }

    /**
     * 2. 多线程生产者消费者示例
     */
    private static void producerConsumerExample() throws InterruptedException {
        System.out.println("=== 2. 多线程生产者消费者示例 ===");

        ModernHighPerformanceMPMCQueue<Integer> queue = QueueFactory.createMediumQueue();
        ExecutorService executor = Executors.newFixedThreadPool(6);
        CountDownLatch latch = new CountDownLatch(6);
        AtomicInteger totalProduced = new AtomicInteger(0);
        AtomicInteger totalConsumed = new AtomicInteger(0);

        // 启动3个生产者
        for (int i = 0; i < 3; i++) {
            final int producerId = i;
            executor.submit(() -> {
                try {
                    // 注册生产者（可选，但建议）
                    queue.registerProducer();

                    for (int j = 0; j < 100; j++) {
                        int value = producerId * 1000 + j;
                        while (!queue.offer(value)) {
                            Thread.onSpinWait(); // 自旋等待
                        }
                        totalProduced.incrementAndGet();

                        if (j % 50 == 0) {
                            System.out.printf("生产者-%d 已生产 %d 个元素\n", producerId, j + 1);
                        }
                    }

                    System.out.printf("生产者-%d 完成\n", producerId);
                } finally {
                    queue.unregisterProducer();
                    latch.countDown();
                }
            });
        }

        // 启动3个消费者
        for (int i = 0; i < 3; i++) {
            final int consumerId = i;
            executor.submit(() -> {
                try {
                    // 注册消费者（可选，但建议）
                    queue.registerConsumer();

                    int consumed = 0;
                    while (consumed < 100) {
                        Integer value = queue.poll();
                        if (value != null) {
                            consumed++;
                            totalConsumed.incrementAndGet();

                            if (consumed % 50 == 0) {
                                System.out.printf("消费者-%d 已消费 %d 个元素\n", consumerId, consumed);
                            }
                        } else {
                            // 短暂等待
                            LockSupport.parkNanos(1000);
                        }
                    }

                    System.out.printf("消费者-%d 完成\n", consumerId);
                } finally {
                    queue.unregisterConsumer();
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.printf("总计: 生产 %d, 消费 %d\n", totalProduced.get(), totalConsumed.get());
        System.out.println("队列统计: " + queue.getStats());
        System.out.println();
    }

    /**
     * 3. 批处理操作示例
     */
    private static void batchOperationsExample() {
        System.out.println("=== 3. 批处理操作示例 ===");

        ModernHighPerformanceMPMCQueue<String> queue = QueueFactory.createSmallQueue();

        // 批量入队
        String[] items = {"batch1", "batch2", "batch3", "batch4", "batch5"};
        int offered = queue.offerBatch(items, 0, items.length);
        System.out.printf("批量入队: 尝试 %d 个，成功 %d 个\n", items.length, offered);

        // 批量出队
        String[] result = new String[3];
        int polled = queue.pollBatch(result, 0, 3);
        System.out.printf("批量出队: 尝试 %d 个，成功 %d 个\n", 3, polled);
        System.out.print("出队结果: ");
        for (int i = 0; i < polled; i++) {
            System.out.print(result[i] + " ");
        }
        System.out.println();

        System.out.println("剩余队列大小: " + queue.size());
        System.out.println();
    }

    /**
     * 4. 阻塞操作示例
     */
    private static void blockingOperationsExample() throws Exception {
        System.out.println("=== 4. 阻塞操作示例 ===");

        ModernHighPerformanceMPMCQueue<String> queue = QueueFactory.createSmallQueue();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 阻塞式生产者
        Future<?> producer = executor.submit(() -> {
            try {
                queue.put("blocking-item-1");
                System.out.println("生产者: 成功put第1个元素");

                Thread.sleep(1000); // 模拟处理时间

                queue.put("blocking-item-2");
                System.out.println("生产者: 成功put第2个元素");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 阻塞式消费者
        Future<?> consumer = executor.submit(() -> {
            try {
                String item1 = queue.take();
                System.out.println("消费者: 获取到 " + item1);

                String item2 = queue.take();
                System.out.println("消费者: 获取到 " + item2);

                // 测试超时方法
                String item3 = queue.poll(500_000_000L); // 500ms超时
                System.out.println("消费者: 超时获取 " + (item3 != null ? item3 : "null"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 等待完成
        producer.get(5, TimeUnit.SECONDS);
        consumer.get(5, TimeUnit.SECONDS);

        executor.shutdown();
        System.out.println();
    }

    /**
     * 5. 队列监控示例
     */
    private static void monitoringExample() throws InterruptedException {
        System.out.println("=== 5. 队列监控示例 ===");

        ModernHighPerformanceMPMCQueue<Integer> queue = QueueFactory.createMediumQueue();
        QueueMonitor monitor = new QueueMonitor(queue);

        // 模拟一些活动
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // 生产者
        executor.submit(() -> {
            queue.registerProducer();
            try {
                for (int i = 0; i < 1000; i++) {
                    queue.offer(i);
                    if (i % 100 == 0) {
                        Thread.sleep(10);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                queue.unregisterProducer();
            }
        });

        // 消费者
        executor.submit(() -> {
            queue.registerConsumer();
            try {
                for (int i = 0; i < 1000; i++) {
                    while (queue.poll() == null) {
                        Thread.sleep(1);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                queue.unregisterConsumer();
            }
        });

        // 监控线程
        for (int i = 0; i < 5; i++) {
            Thread.sleep(500);
            System.out.println("监控报告: " + monitor.getRealTimeStats());
            System.out.println("健康状态: " + monitor.getHealthReport());
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 最终分析
        System.out.println("\n=== 最终性能分析 ===");
        QueueAnalyzer.analyzePerformance(queue);
        System.out.println(QueueAnalyzer.getPerformanceAdvice(queue));
    }
}
