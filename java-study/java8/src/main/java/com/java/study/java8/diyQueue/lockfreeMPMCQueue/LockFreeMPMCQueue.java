package com.java.study.java8.diyQueue.lockfreeMPMCQueue;

import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.LockSupport;

/**
 * 简化的高性能无锁多生产者多消费者队列
 * 基于Michael & Scott算法，避免了复杂的扩容逻辑
 */
public class LockFreeMPMCQueue<T> {

    // 队列节点
    private static class Node<T> {
        volatile T item;
        final AtomicReference<Node<T>> next = new AtomicReference<>();

        Node(T item) {
            this.item = item;
        }
    }

    // 队列头尾指针
    private final AtomicReference<Node<T>> head;
    private final AtomicReference<Node<T>> tail;

    // 统计信息
    private final AtomicInteger producerCount = new AtomicInteger(0);
    private final AtomicInteger consumerCount = new AtomicInteger(0);
    private final AtomicLong totalEnqueued = new AtomicLong(0);
    private final AtomicLong totalDequeued = new AtomicLong(0);
    private final AtomicInteger currentSize = new AtomicInteger(0);

    // 性能监控
    private final AtomicLong offerRetries = new AtomicLong(0);
    private final AtomicLong pollRetries = new AtomicLong(0);

    // 背压控制
    private volatile int maxSize = 1000000; // 默认最大100万元素

    public LockFreeMPMCQueue() {
        Node<T> dummy = new Node<>(null);
        head = new AtomicReference<>(dummy);
        tail = new AtomicReference<>(dummy);
    }

    public LockFreeMPMCQueue(int maxSize) {
        this();
        this.maxSize = maxSize;
    }

    /**
     * 注册生产者
     */
    public void registerProducer() {
        producerCount.incrementAndGet();
    }

    /**
     * 注销生产者
     */
    public void unregisterProducer() {
        producerCount.decrementAndGet();
    }

    /**
     * 注册消费者
     */
    public void registerConsumer() {
        consumerCount.incrementAndGet();
    }

    /**
     * 注销消费者
     */
    public void unregisterConsumer() {
        consumerCount.decrementAndGet();
    }

    /**
     * 入队操作 - 非阻塞
     */
    public boolean offer(T item) {
        if (item == null) {
            throw new NullPointerException("Item cannot be null");
        }

        // 简单的背压检查
        if (currentSize.get() >= maxSize) {
            return false;
        }

        Node<T> newNode = new Node<>(item);

        while (true) {
            Node<T> last = tail.get();
            Node<T> next = last.next.get();

            // 检查tail是否依然指向最后一个节点
            if (last == tail.get()) {
                if (next == null) {
                    // tail确实指向最后一个节点，尝试链接新节点
                    if (last.next.compareAndSet(null, newNode)) {
                        // 成功链接，现在尝试移动tail
                        tail.compareAndSet(last, newNode);
                        currentSize.incrementAndGet();
                        totalEnqueued.incrementAndGet();
                        return true;
                    }
                } else {
                    // tail没有指向最后一个节点，尝试移动tail
                    tail.compareAndSet(last, next);
                }
            }

            // 记录重试次数
            offerRetries.incrementAndGet();

            // 短暂让出CPU，避免过度竞争
            if (offerRetries.get() % 100 == 0) {
                Thread.yield();
            }
        }
    }

    /**
     * 出队操作 - 非阻塞
     */
    public T poll() {
        while (true) {
            Node<T> first = head.get();
            Node<T> last = tail.get();
            Node<T> next = first.next.get();

            // 检查head是否依然指向第一个节点
            if (first == head.get()) {
                if (first == last) {
                    if (next == null) {
                        // 队列为空
                        return null;
                    }
                    // tail落后了，尝试推进tail
                    tail.compareAndSet(last, next);
                } else {
                    if (next == null) {
                        // 不应该发生，继续重试
                        continue;
                    }

                    // 读取数据
                    T item = next.item;

                    // 尝试移动head
                    if (head.compareAndSet(first, next)) {
                        // 成功，清理数据避免内存泄漏
                        if (item != null) {
                            currentSize.decrementAndGet();
                            totalDequeued.incrementAndGet();
                        }
                        return item;
                    }
                }
            }

            // 记录重试次数
            pollRetries.incrementAndGet();

            // 短暂让出CPU，避免过度竞争
            if (pollRetries.get() % 100 == 0) {
                Thread.yield();
            }
        }
    }

    /**
     * 阻塞式入队
     */
    public void put(T item) throws InterruptedException {
        while (!offer(item)) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            LockSupport.parkNanos(1000); // 1微秒
        }
    }

    /**
     * 阻塞式出队
     */
    public T take() throws InterruptedException {
        T item;
        while ((item = poll()) == null) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            LockSupport.parkNanos(1000); // 1微秒
        }
        return item;
    }

    /**
     * 带超时的出队
     */
    public T poll(long timeoutNanos) {
        long deadline = System.nanoTime() + timeoutNanos;
        T item;

        while ((item = poll()) == null) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                return null;
            }
            LockSupport.parkNanos(Math.min(remaining, 1000));
        }
        return item;
    }

    /**
     * 获取队列大小（近似值）
     */
    public int size() {
        return Math.max(0, currentSize.get());
    }

    /**
     * 检查队列是否为空
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * 清空队列
     */
    public void clear() {
        while (poll() != null) {
            // 继续清空
        }
    }

    /**
     * 获取详细统计信息
     */
    public String getStats() {
        long enqueued = totalEnqueued.get();
        long dequeued = totalDequeued.get();

        return String.format(
                "Queue Stats: size=%d, producers=%d, consumers=%d, " +
                        "enqueued=%d, dequeued=%d, pending=%d, " +
                        "offer_retries=%d, poll_retries=%d, max_size=%d",
                size(), producerCount.get(), consumerCount.get(),
                enqueued, dequeued, enqueued - dequeued,
                offerRetries.get(), pollRetries.get(), maxSize
        );
    }

    /**
     * 重置统计信息
     */
    public void resetStats() {
        totalEnqueued.set(0);
        totalDequeued.set(0);
        offerRetries.set(0);
        pollRetries.set(0);
    }

    /**
     * 设置最大大小
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * 获取最大大小
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * 获取竞争度（重试次数越多说明竞争越激烈）
     */
    public double getContentionLevel() {
        long totalOps = totalEnqueued.get() + totalDequeued.get();
        long totalRetries = offerRetries.get() + pollRetries.get();
        return totalOps > 0 ? (double) totalRetries / totalOps : 0.0;
    }
}