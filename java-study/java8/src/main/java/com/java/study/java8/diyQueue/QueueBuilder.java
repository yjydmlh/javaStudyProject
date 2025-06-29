package com.java.study.java8.diyQueue;

/**
 * 现代高性能队列构建器
 * 提供链式调用方式创建队列
 */
public class QueueBuilder<T> {
    private int capacity = 1024 * 1024; // 默认1M容量

    /**
     * 创建新的构建器实例
     *
     * @param <T> 队列元素类型
     * @return 构建器实例
     */
    public static <T> QueueBuilder<T> newBuilder() {
        return new QueueBuilder<>();
    }

    /**
     * 设置队列容量
     *
     * @param capacity 容量大小，会自动调整为2的幂
     * @return 构建器实例
     */
    public QueueBuilder<T> capacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        return this;
    }

    /**
     * 构建队列实例
     *
     * @return 配置好的队列实例
     */
    public ModernHighPerformanceMPMCQueue<T> build() {
        return new ModernHighPerformanceMPMCQueue<>(capacity);
    }
}

/**
 * 队列工厂类
 * 提供预配置的队列实例
 */
class QueueFactory {

    /**
     * 创建小容量队列（适合低延迟场景）
     */
    public static <T> ModernHighPerformanceMPMCQueue<T> createSmallQueue() {
        return new ModernHighPerformanceMPMCQueue<>(1024);
    }

    /**
     * 创建中等容量队列（适合普通并发场景）
     */
    public static <T> ModernHighPerformanceMPMCQueue<T> createMediumQueue() {
        return new ModernHighPerformanceMPMCQueue<>(64 * 1024);
    }

    /**
     * 创建大容量队列（适合高吞吐量场景）
     */
    public static <T> ModernHighPerformanceMPMCQueue<T> createLargeQueue() {
        return new ModernHighPerformanceMPMCQueue<>(1024 * 1024);
    }

    /**
     * 创建超大容量队列（适合极高吞吐量场景）
     */
    public static <T> ModernHighPerformanceMPMCQueue<T> createExtraLargeQueue() {
        return new ModernHighPerformanceMPMCQueue<>(16 * 1024 * 1024);
    }
}

/**
 * 队列性能分析工具
 */
class QueueAnalyzer {

    /**
     * 分析队列性能指标
     */
    public static void analyzePerformance(ModernHighPerformanceMPMCQueue<?> queue) {
        System.out.println("=== 队列性能分析 ===");
        System.out.println(queue.getStats());

        double contentionLevel = queue.getContentionLevel();
        if (contentionLevel < 0.01) {
            System.out.println("✅ 竞争度很低，性能良好");
        } else if (contentionLevel < 0.05) {
            System.out.println("⚠️  竞争度中等，可考虑优化");
        } else {
            System.out.println("❌ 竞争度高，建议增加容量或减少并发");
        }

        int utilizationPercent = (int) ((double) queue.size() / queue.capacity() * 100);
        System.out.printf("队列使用率: %d%%\n", utilizationPercent);

        if (utilizationPercent > 80) {
            System.out.println("⚠️  队列使用率过高，建议增加容量");
        }
    }

    /**
     * 获取性能建议
     */
    public static String getPerformanceAdvice(ModernHighPerformanceMPMCQueue<?> queue) {
        StringBuilder advice = new StringBuilder();
        advice.append("性能建议:\n");

        double contentionLevel = queue.getContentionLevel();
        if (contentionLevel > 0.05) {
            advice.append("- 竞争度过高，考虑增加队列容量\n");
            advice.append("- 或者使用多个小队列分片\n");
        }

        int size = queue.size();
        int capacity = queue.capacity();
        double utilization = (double) size / capacity;

        if (utilization > 0.8) {
            advice.append("- 队列接近满载，建议增加容量\n");
        } else if (utilization < 0.1 && capacity > 1024) {
            advice.append("- 队列使用率低，可考虑减少容量以节省内存\n");
        }

        if (queue.isEmpty()) {
            advice.append("- 队列为空，检查生产者是否正常工作\n");
        }

        return advice.toString();
    }
}

/**
 * 队列监控器
 * 用于实时监控队列状态
 */
class QueueMonitor {
    private final ModernHighPerformanceMPMCQueue<?> queue;
    private final long startTime;

    public QueueMonitor(ModernHighPerformanceMPMCQueue<?> queue) {
        this.queue = queue;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 获取实时统计
     */
    public String getRealTimeStats() {
        long runTime = System.currentTimeMillis() - startTime;
        return String.format(
                "运行时间: %d秒, %s",
                runTime / 1000,
                queue.getStats()
        );
    }

    /**
     * 检查队列健康状态
     */
    public boolean isHealthy() {
        double contentionLevel = queue.getContentionLevel();
        double utilization = (double) queue.size() / queue.capacity();

        return contentionLevel < 0.1 && utilization < 0.9;
    }

    /**
     * 获取健康状态报告
     */
    public String getHealthReport() {
        boolean healthy = isHealthy();
        double contentionLevel = queue.getContentionLevel();
        double utilization = (double) queue.size() / queue.capacity();

        return String.format(
                "健康状态: %s, 竞争度: %.4f, 使用率: %.1f%%",
                healthy ? "良好" : "需要关注",
                contentionLevel,
                utilization * 100
        );
    }
}