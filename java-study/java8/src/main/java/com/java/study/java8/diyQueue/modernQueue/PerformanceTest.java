package com.java.study.java8.diyQueue.modernQueue;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.Arrays;
import java.util.Random;

/**
 * ModernHighPerformanceMPMCQueue 性能测试
 * 包含吞吐量、延迟、批处理等各种性能测试
 */
public class PerformanceTest {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== ModernHighPerformanceMPMCQueue 性能测试 ===");
        System.out.println("Java版本: " + System.getProperty("java.version"));
        System.out.println("JVM: " + System.getProperty("java.vm.name"));
        System.out.println("可用处理器: " + Runtime.getRuntime().availableProcessors());
        System.out.println();

        // 预热JVM
        warmupJVM();

        // 1. 单线程性能基准测试
        singleThreadBenchmark();

        // 2. 多线程吞吐量测试
        multiThreadThroughputTest();

        // 3. 延迟分布测试
        latencyDistributionTest();

        // 4. 批处理性能测试
        batchPerformanceTest();

        // 5. 压力测试
        stressTest();

        // 6. 内存使用分析
        memoryUsageAnalysis();

        System.out.println("=== 性能测试完成 ===");
    }

    /**
     * JVM预热
     */
    private static void warmupJVM() throws InterruptedException {
        System.out.println("=== JVM预热 ===");

        ModernHighPerformanceMPMCQueue<Integer> warmupQueue = new ModernHighPerformanceMPMCQueue<>(1024);

        // 预热轮次
        for (int round = 0; round < 3; round++) {
            System.out.printf("预热轮次 %d/3\n", round + 1);

            // 单线程预热
            for (int i = 0; i < 100000; i++) {
                warmupQueue.offer(i);
                warmupQueue.poll();
            }

            // 多线程预热
            ExecutorService executor = Executors.newFixedThreadPool(4);
            CountDownLatch latch = new CountDownLatch(4);

            for (int i = 0; i < 4; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < 10000; j++) {
                            warmupQueue.offer(j);
                            warmupQueue.poll();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            System.gc();
            Thread.sleep(500);
        }

        System.out.println("预热完成\n");
    }

    /**
     * 1. 单线程性能基准测试
     */
    private static void singleThreadBenchmark() {
        System.out.println("=== 1. 单线程性能基准测试 ===");

        final int iterations = 100_000_000; // 1000万次操作
        ModernHighPerformanceMPMCQueue<Integer> queue = new ModernHighPerformanceMPMCQueue<>(iterations);

        // 测试入队性能
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            queue.offer(i);
        }
        long offerTime = System.nanoTime() - startTime;

        // 测试出队性能
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            queue.poll();
        }
        long pollTime = System.nanoTime() - startTime;

        double offerThroughput = iterations * 1_000_000_000.0 / offerTime;
        double pollThroughput = iterations * 1_000_000_000.0 / pollTime;
        double totalThroughput = (iterations * 2) * 1_000_000_000.0 / (offerTime + pollTime);

        System.out.printf("单线程性能 (%,d 操作):\n", iterations);
        System.out.printf("  Offer吞吐量: %,.2f M ops/s\n", offerThroughput / 1_000_000);
        System.out.printf("  Poll吞吐量:  %,.2f M ops/s\n", pollThroughput / 1_000_000);
        System.out.printf("  总体吞吐量: %,.2f M ops/s\n", totalThroughput / 1_000_000);
        System.out.printf("  平均延迟:   %.2f ns/op\n", (offerTime + pollTime) / (double) (iterations * 2));
        System.out.println();
    }

    /**
     * 2. 多线程吞吐量测试
     */
    private static void multiThreadThroughputTest() throws InterruptedException {
        System.out.println("=== 2. 多线程吞吐量测试 ===");

        int[] threadConfigs = {2, 4, 8, 16};
        final int messagesPerThread = 10_000_000;

        for (int totalThreads : threadConfigs) {
            int producers = totalThreads / 2;
            int consumers = totalThreads / 2;

            System.out.printf("--- %d生产者 x %d消费者 ---\n", producers, consumers);

            ThroughputResult result = runThroughputTest(producers, consumers, messagesPerThread);

            System.out.printf("总操作数: %,d\n", result.totalOperations);
            System.out.printf("耗时: %.3f 秒\n", result.durationSeconds);
            System.out.printf("吞吐量: %,.2f M ops/s\n", result.throughputMOps);
            System.out.printf("队列统计: %s\n", result.queueStats);
            System.out.println();
        }
    }

    /**
     * 执行吞吐量测试
     */
    private static ThroughputResult runThroughputTest(int producers, int consumers, int messagesPerProducer)
            throws InterruptedException {

        ModernHighPerformanceMPMCQueue<Integer> queue = new ModernHighPerformanceMPMCQueue<>(messagesPerProducer * producers);
        ExecutorService executor = Executors.newFixedThreadPool(producers + consumers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch producerLatch = new CountDownLatch(producers);
        AtomicLong totalProduced = new AtomicLong(0);
        AtomicLong totalConsumed = new AtomicLong(0);
        AtomicBoolean testComplete = new AtomicBoolean(false);

        // 注册生产者和消费者
        for (int i = 0; i < producers; i++) queue.registerProducer();
        for (int i = 0; i < consumers; i++) queue.registerConsumer();

        long startTime = System.nanoTime();

        // 启动生产者
        for (int i = 0; i < producers; i++) {
            final int producerId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    Random random = new Random(producerId);

                    for (int j = 0; j < messagesPerProducer && !testComplete.get(); j++) {
                        while (!queue.offer(random.nextInt()) && !testComplete.get()) {
                            Thread.onSpinWait();
                        }
                        totalProduced.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    producerLatch.countDown();
                }
            });
        }

        // 启动消费者
        for (int i = 0; i < consumers; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();

                    while (!testComplete.get()) {
                        Integer item = queue.poll();
                        if (item != null) {
                            totalConsumed.incrementAndGet();
                        } else {
                            if (producerLatch.getCount() == 0 && totalConsumed.get() >= totalProduced.get()) {
                                break;
                            }
                            Thread.onSpinWait();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // 开始测试
        startLatch.countDown();

        // 等待生产者完成
        producerLatch.await(60, TimeUnit.SECONDS);

        // 等待消费者完成
        long deadline = System.currentTimeMillis() + 60000;
        while (totalConsumed.get() < totalProduced.get() && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }

        testComplete.set(true);
        long endTime = System.nanoTime();

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        long duration = endTime - startTime;
        long totalOps = totalProduced.get() + totalConsumed.get();
        double durationSeconds = duration / 1_000_000_000.0;
        double throughput = totalOps / durationSeconds;

        return new ThroughputResult(
                totalOps, durationSeconds, throughput / 1_000_000, queue.getStats()
        );
    }

    /**
     * 3. 延迟分布测试
     */
    private static void latencyDistributionTest() {
        System.out.println("=== 3. 延迟分布测试 ===");

        final int iterations = 100_000_000;
        ModernHighPerformanceMPMCQueue<Integer> queue = new ModernHighPerformanceMPMCQueue<>(1024);
        long[] latencies = new long[iterations];

        // 预热
        for (int i = 0; i < 100000; i++) {
            queue.offer(i);
            queue.poll();
        }

        // 测量往返延迟
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            queue.offer(i);
            queue.poll();
            long end = System.nanoTime();
            latencies[i] = end - start;
        }

        // 分析延迟分布
        Arrays.sort(latencies);

        System.out.printf("延迟分布 (%,d 次往返操作):\n", iterations);
        System.out.printf("  最小值: %,d ns\n", latencies[0]);
        System.out.printf("  P50:    %,d ns\n", latencies[iterations / 2]);
        System.out.printf("  P90:    %,d ns\n", latencies[(int) (iterations * 0.9)]);
        System.out.printf("  P95:    %,d ns\n", latencies[(int) (iterations * 0.95)]);
        System.out.printf("  P99:    %,d ns\n", latencies[(int) (iterations * 0.99)]);
        System.out.printf("  P99.9:  %,d ns\n", latencies[(int) (iterations * 0.999)]);
        System.out.printf("  最大值: %,d ns\n", latencies[iterations - 1]);

        double average = Arrays.stream(latencies).average().orElse(0.0);
        System.out.printf("  平均值: %,.2f ns\n", average);
        System.out.println();
    }

    /**
     * 4. 批处理性能测试
     */
    private static void batchPerformanceTest() {
        System.out.println("=== 4. 批处理性能测试 ===");

        final int totalElements = 10_000_000;
        final int[] batchSizes = {1, 10, 50, 100, 500};

        for (int batchSize : batchSizes) {
            ModernHighPerformanceMPMCQueue<Integer> queue = new ModernHighPerformanceMPMCQueue<>(totalElements);

            // 准备批处理数据
            Integer[] batchData = new Integer[batchSize];
            for (int i = 0; i < batchSize; i++) {
                batchData[i] = i;
            }

            int batches = totalElements / batchSize;

            // 测试批量入队
            long startTime = System.nanoTime();
            for (int i = 0; i < batches; i++) {
                queue.offerBatch(batchData, 0, batchSize);
            }
            long batchOfferTime = System.nanoTime() - startTime;

            // 测试批量出队
            Integer[] resultBatch = new Integer[batchSize];
            startTime = System.nanoTime();
            for (int i = 0; i < batches; i++) {
                queue.pollBatch(resultBatch, 0, batchSize);
            }
            long batchPollTime = System.nanoTime() - startTime;

            double totalTime = (batchOfferTime + batchPollTime) / 1_000_000_000.0;
            double throughput = (totalElements * 2) / totalTime;

            System.out.printf("批大小 %3d: %,.2f M ops/s\n", batchSize, throughput / 1_000_000);
        }
        System.out.println();
    }

    /**
     * 5. 压力测试
     */
    private static void stressTest() throws InterruptedException {
        System.out.println("=== 5. 压力测试 ===");

        final int duration = 30; // 30秒压力测试
        final int producers = 8;
        final int consumers = 8;

        ModernHighPerformanceMPMCQueue<Integer> queue = new ModernHighPerformanceMPMCQueue<>(1024 * 1024);
        ExecutorService executor = Executors.newFixedThreadPool(producers + consumers);
        AtomicLong totalOperations = new AtomicLong(0);
        AtomicBoolean running = new AtomicBoolean(true);

        // 注册生产者和消费者
        for (int i = 0; i < producers; i++) queue.registerProducer();
        for (int i = 0; i < consumers; i++) queue.registerConsumer();

        // 启动生产者
        for (int i = 0; i < producers; i++) {
            final int producerId = i;
            executor.submit(() -> {
                Random random = new Random(producerId);
                while (running.get()) {
                    if (queue.offer(random.nextInt())) {
                        totalOperations.incrementAndGet();
                    }
                }
            });
        }

        // 启动消费者
        for (int i = 0; i < consumers; i++) {
            executor.submit(() -> {
                while (running.get()) {
                    if (queue.poll() != null) {
                        totalOperations.incrementAndGet();
                    }
                }
            });
        }

        // 运行指定时间
        System.out.printf("运行 %d 秒压力测试...\n", duration);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < duration; i++) {
            Thread.sleep(1000);
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            long ops = totalOperations.get();
            System.out.printf("第 %2d 秒: %,d 总操作, %,.2f M ops/s, 队列大小: %d\n",
                    elapsed, ops, ops / (double) elapsed / 1_000_000, queue.size());
        }

        running.set(false);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        long totalOps = totalOperations.get();
        double avgThroughput = totalOps / (double) duration / 1_000_000;

        System.out.printf("\n压力测试结果:\n");
        System.out.printf("  总操作数: %,d\n", totalOps);
        System.out.printf("  平均吞吐量: %,.2f M ops/s\n", avgThroughput);
        System.out.printf("  最终队列状态: %s\n", queue.getStats());
        System.out.printf("  竞争度: %.6f\n", queue.getContentionLevel());
        System.out.println();
    }

    /**
     * 6. 内存使用分析
     */
    private static void memoryUsageAnalysis() {
        System.out.println("=== 6. 内存使用分析 ===");

        Runtime runtime = Runtime.getRuntime();
        int[] capacities = {1024, 10240, 102400, 1024000};

        for (int capacity : capacities) {
            // 强制GC
            System.gc();
            System.gc();
            Thread.yield();

            long memBefore = runtime.totalMemory() - runtime.freeMemory();

            // 创建队列
            ModernHighPerformanceMPMCQueue<Integer> queue = new ModernHighPerformanceMPMCQueue<>(capacity);

            // 填充50%容量
            for (int i = 0; i < capacity / 2; i++) {
                queue.offer(i);
            }

            // 再次测量
            System.gc();
            System.gc();
            Thread.yield();

            long memAfter = runtime.totalMemory() - runtime.freeMemory();
            long memUsed = memAfter - memBefore;

            System.out.printf("容量 %,7d: 内存使用 %,d KB, 每元素 %d bytes\n",
                    capacity, memUsed / 1024, memUsed / (capacity / 2));
        }
        System.out.println();
    }

    // 结果类
    static class ThroughputResult {
        final long totalOperations;
        final double durationSeconds;
        final double throughputMOps;
        final String queueStats;

        ThroughputResult(long totalOperations, double durationSeconds, double throughputMOps, String queueStats) {
            this.totalOperations = totalOperations;
            this.durationSeconds = durationSeconds;
            this.throughputMOps = throughputMOps;
            this.queueStats = queueStats;
        }
    }
}
