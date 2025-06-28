package com.java.study.java8.diyQueue;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * 测试LockFreeMPMCQueue
 */
public class LockFreeQueueUsageExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("开始测试无锁MPMC队列...\n");

        // 基本使用示例
        basicUsageExample();

        Thread.sleep(1000);

        // 性能测试
        performanceTest();

        Thread.sleep(1000);

        // 动态负载测试
        dynamicLoadTest();

        Thread.sleep(1000);

        // 对比测试
        comparisonTest();
    }

    /**
     * 基本使用示例
     */
    private static void basicUsageExample() throws InterruptedException {
        System.out.println("=== 基本使用示例 ===");

        LockFreeMPMCQueue<String> queue = new LockFreeMPMCQueue<>(100);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(20); // 2个生产者各10条 + 2个消费者各接收

        // 启动2个生产者
        for (int i = 0; i < 2; i++) {
            final int producerId = i;
            executor.submit(() -> {
                queue.registerProducer();
                try {
                    for (int j = 0; j < 10; j++) {
                        String message = "Producer-" + producerId + "-Message-" + j;
                        if (queue.offer(message)) {
                            System.out.println("✓ 生产: " + message);
                            latch.countDown();
                        }
                        Thread.sleep(50);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    queue.unregisterProducer();
                }
            });
        }

        // 启动2个消费者，等待一段时间后开始消费
        Thread.sleep(100);
        for (int i = 0; i < 2; i++) {
            final int consumerId = i;
            executor.submit(() -> {
                queue.registerConsumer();
                try {
                    while (latch.getCount() > 0) {
                        String message = queue.poll();
                        if (message != null) {
                            System.out.println("✓ 消费者-" + consumerId + " 消费: " + message);
                        } else {
                            Thread.sleep(10);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    queue.unregisterConsumer();
                }
            });
        }

        // 等待完成
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("基本测试完成: " + queue.getStats());
        System.out.println();
    }

    /**
     * 性能测试 - 修复版本
     */
    private static void performanceTest() throws InterruptedException {
        System.out.println("=== 性能测试 ===");

        final int PRODUCERS = 2;
        final int CONSUMERS = 2;
        final int MESSAGES_PER_PRODUCER = 50000;
        final int TOTAL_MESSAGES = PRODUCERS * MESSAGES_PER_PRODUCER;

        LockFreeMPMCQueue<Integer> queue = new LockFreeMPMCQueue<>(TOTAL_MESSAGES / 2);
        ExecutorService executor = Executors.newFixedThreadPool(PRODUCERS + CONSUMERS);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch producerLatch = new CountDownLatch(PRODUCERS);
        AtomicLong totalConsumed = new AtomicLong(0);
        AtomicBoolean testComplete = new AtomicBoolean(false);

        long startTime = System.nanoTime();

        // 启动生产者
        for (int i = 0; i < PRODUCERS; i++) {
            final int producerId = i;
            executor.submit(() -> {
                queue.registerProducer();
                try {
                    startLatch.await();
                    Random random = new Random(producerId);
                    int produced = 0;

                    for (int j = 0; j < MESSAGES_PER_PRODUCER; j++) {
                        while (!queue.offer(random.nextInt()) && !testComplete.get()) {
                            LockSupport.parkNanos(100);
                        }
                        produced++;

                        // 每生产1000个打印一次进度
                        if (produced % 10000 == 0) {
                            System.out.printf("生产者-%d 已生产 %d/%d\n",
                                    producerId, produced, MESSAGES_PER_PRODUCER);
                        }
                    }
                    System.out.printf("生产者-%d 完成，共生产 %d 条消息\n", producerId, produced);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    queue.unregisterProducer();
                    producerLatch.countDown();
                }
            });
        }

        // 启动消费者
        for (int i = 0; i < CONSUMERS; i++) {
            final int consumerId = i;
            executor.submit(() -> {
                queue.registerConsumer();
                try {
                    startLatch.await();
                    int consumed = 0;

                    while (true) {
                        Integer value = queue.poll();
                        if (value != null) {
                            consumed++;
                            totalConsumed.incrementAndGet();

                            // 每消费1000个打印一次进度
                            if (consumed % 10000 == 0) {
                                System.out.printf("消费者-%d 已消费 %d，总消费 %d/%d\n",
                                        consumerId, consumed, totalConsumed.get(), TOTAL_MESSAGES);
                            }
                        } else {
                            // 检查是否所有生产者都完成了
                            if (producerLatch.getCount() == 0 && queue.isEmpty()) {
                                break;
                            }
                            LockSupport.parkNanos(1000);
                        }

                        // 如果已经消费了足够的消息，退出
                        if (totalConsumed.get() >= TOTAL_MESSAGES) {
                            break;
                        }
                    }
                    System.out.printf("消费者-%d 完成，共消费 %d 条消息\n", consumerId, consumed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    queue.unregisterConsumer();
                }
            });
        }

        // 开始测试
        System.out.println("开始性能测试...");
        startLatch.countDown();

        // 等待所有生产者完成
        producerLatch.await(30, TimeUnit.SECONDS);

        // 等待消费者完成或超时
        long deadline = System.currentTimeMillis() + 30000; // 30秒超时
        while (totalConsumed.get() < TOTAL_MESSAGES && System.currentTimeMillis() < deadline) {
            Thread.sleep(100);
        }

        testComplete.set(true);
        long endTime = System.nanoTime();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        long totalTime = endTime - startTime;
        long finalConsumed = totalConsumed.get();
        double seconds = totalTime / 1_000_000_000.0;
        double throughput = (TOTAL_MESSAGES + finalConsumed) / seconds;

        System.out.printf("\n性能测试结果:\n");
        System.out.printf("  生产者数量: %d\n", PRODUCERS);
        System.out.printf("  消费者数量: %d\n", CONSUMERS);
        System.out.printf("  目标消息数: %d\n", TOTAL_MESSAGES);
        System.out.printf("  实际消费数: %d\n", finalConsumed);
        System.out.printf("  总耗时: %.2f 秒\n", seconds);
        System.out.printf("  吞吐量: %.0f 操作/秒\n", throughput);
        System.out.printf("  队列统计: %s\n", queue.getStats());
        System.out.printf("  竞争度: %.2f\n", queue.getContentionLevel());
        System.out.println();
    }

    /**
     * 动态负载测试
     */
    private static void dynamicLoadTest() throws InterruptedException {
        System.out.println("=== 动态负载测试 ===");

        LockFreeMPMCQueue<String> queue = new LockFreeMPMCQueue<>(1000);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        AtomicBoolean running = new AtomicBoolean(true);

        // 监控线程
        Future<?> monitor = executor.submit(() -> {
            try {
                while (running.get()) {
                    System.out.println("监控: " + queue.getStats());
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 阶段1: 轻负载 - 1个生产者，1个消费者
        System.out.println("阶段1: 轻负载测试 (1生产者, 1消费者)");
        runLoadPhase(queue, executor, 1, 1, 1000, 5000);

        Thread.sleep(2000);

        // 阶段2: 中等负载 - 2个生产者，2个消费者
        System.out.println("阶段2: 中等负载测试 (2生产者, 2消费者)");
        runLoadPhase(queue, executor, 2, 2, 2000, 10000);

        Thread.sleep(2000);

        // 阶段3: 高负载 - 4个生产者，3个消费者
        System.out.println("阶段3: 高负载测试 (4生产者, 3消费者)");
        runLoadPhase(queue, executor, 4, 3, 3000, 15000);

        running.set(false);
        monitor.cancel(true);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("动态负载测试完成");
        System.out.println();
    }

    /**
     * 运行负载测试阶段
     */
    private static void runLoadPhase(LockFreeMPMCQueue<String> queue, ExecutorService executor,
                                     int producers, int consumers, int messagesPerProducer, int duration)
            throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(producers + consumers);
        AtomicBoolean phaseRunning = new AtomicBoolean(true);

        // 启动生产者
        for (int i = 0; i < producers; i++) {
            final int id = i;
            executor.submit(() -> {
                queue.registerProducer();
                try {
                    Random random = new Random();
                    int count = 0;
                    while (phaseRunning.get() && count < messagesPerProducer) {
                        if (queue.offer("P" + id + "-" + count)) {
                            count++;
                        }
                        Thread.sleep(random.nextInt(10));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    queue.unregisterProducer();
                    latch.countDown();
                }
            });
        }

        // 启动消费者
        for (int i = 0; i < consumers; i++) {
            final int id = i;
            executor.submit(() -> {
                queue.registerConsumer();
                try {
                    Random random = new Random();
                    while (phaseRunning.get()) {
                        String msg = queue.poll();
                        if (msg == null) {
                            Thread.sleep(1);
                        } else {
                            Thread.sleep(random.nextInt(5));
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    queue.unregisterConsumer();
                    latch.countDown();
                }
            });
        }

        // 运行指定时间
        Thread.sleep(duration);
        phaseRunning.set(false);
        latch.await(5, TimeUnit.SECONDS);
    }

    /**
     * 对比测试
     */
    private static void comparisonTest() throws InterruptedException {
        System.out.println("=== 与标准队列的性能对比 ===");

        final int ITERATIONS = 100000;
        final int THREADS = 4;

        // 测试我们的无锁队列
        long lockFreeTime = benchmarkQueue("无锁队列", () -> new LockFreeMPMCQueue<>(), ITERATIONS, THREADS);

        // 测试ConcurrentLinkedQueue
        long clqTime = benchmarkQueue("ConcurrentLinkedQueue", () -> new ConcurrentLinkedQueue<>(), ITERATIONS, THREADS);

        // 测试LinkedBlockingQueue
        long lbqTime = benchmarkQueue("LinkedBlockingQueue", () -> new LinkedBlockingQueue<>(), ITERATIONS, THREADS);

        System.out.println("\n对比结果:");
        System.out.printf("无锁队列: %d ms (基准)\n", lockFreeTime);
        System.out.printf("ConcurrentLinkedQueue: %d ms (%.2fx)\n", clqTime, (double)clqTime / lockFreeTime);
        System.out.printf("LinkedBlockingQueue: %d ms (%.2fx)\n", lbqTime, (double)lbqTime / lockFreeTime);
    }

    @FunctionalInterface
    interface QueueSupplier {
        Object get();
    }

    private static long benchmarkQueue(String name, QueueSupplier supplier, int iterations, int threads)
            throws InterruptedException {
        Object queue = supplier.get();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicLong operations = new AtomicLong(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    Random random = new Random(threadId);
                    for (int j = 0; j < iterations / threads; j++) {
                        if (threadId % 2 == 0) {
                            // 生产者
                            offerToQueue(queue, random.nextInt());
                            operations.incrementAndGet();
                        } else {
                            // 消费者
                            pollFromQueue(queue);
                            operations.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        long duration = endTime - startTime;
        System.out.printf("%s: %d ms, %d 操作\n", name, duration, operations.get());
        return duration;
    }

    @SuppressWarnings("unchecked")
    private static void offerToQueue(Object queue, Integer value) {
        if (queue instanceof LockFreeMPMCQueue) {
            ((LockFreeMPMCQueue<Integer>) queue).offer(value);
        } else if (queue instanceof ConcurrentLinkedQueue) {
            ((ConcurrentLinkedQueue<Integer>) queue).offer(value);
        } else if (queue instanceof LinkedBlockingQueue) {
            ((LinkedBlockingQueue<Integer>) queue).offer(value);
        }
    }

    @SuppressWarnings("unchecked")
    private static Integer pollFromQueue(Object queue) {
        if (queue instanceof LockFreeMPMCQueue) {
            return ((LockFreeMPMCQueue<Integer>) queue).poll();
        } else if (queue instanceof ConcurrentLinkedQueue) {
            return ((ConcurrentLinkedQueue<Integer>) queue).poll();
        } else if (queue instanceof LinkedBlockingQueue) {
            return ((LinkedBlockingQueue<Integer>) queue).poll();
        }
        return null;
    }

}
