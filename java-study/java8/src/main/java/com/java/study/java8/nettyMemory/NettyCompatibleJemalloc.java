package com.java.study.java8.nettyMemory;

import jdk.incubator.foreign.*;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 完整可编译的Netty兼容JavaJemalloc实现
 * 编译命令：javac
 * 运行命令：java --add-modules jdk.incubator.foreign --enable-preview NettyCompatibleJemalloc
 */
public class NettyCompatibleJemalloc {

    // ===============================================
    // 1. 基础数据结构定义
    // ===============================================

    /**
     * 内存句柄 - 表示一个已分配的内存块
     */
    public static class MemoryHandle {
        private final Arena arena;
        private final long offset;
        private final long size;
        private final long allocatedTime;

        public MemoryHandle(Arena arena, long offset, long size) {
            this.arena = arena;
            this.offset = offset;
            this.size = size;
            this.allocatedTime = System.currentTimeMillis();
        }

        public MemorySegment getMemorySegment() {
            return arena.getMemorySegment(offset, size);
        }

        public Arena getArena() {
            return arena;
        }

        public long getOffset() {
            return offset;
        }

        public long getSize() {
            return size;
        }

        public long getAllocatedTime() {
            return allocatedTime;
        }

        @Override
        public String toString() {
            return String.format("MemoryHandle{offset=%d, size=%d, arena=%d}",
                    offset, size, arena.getArenaId());
        }
    }

    /**
     * Arena统计信息
     */
    public static class ArenaStats {
        public final long allocated;
        public final long deallocated;
        public final long chunksCount;
        public final long freeChunks;

        public ArenaStats(long allocated, long deallocated, long chunksCount, long freeChunks) {
            this.allocated = allocated;
            this.deallocated = deallocated;
            this.chunksCount = chunksCount;
            this.freeChunks = freeChunks;
        }

        @Override
        public String toString() {
            return String.format("ArenaStats{allocated=%d, deallocated=%d, chunks=%d, free=%d}",
                    allocated, deallocated, chunksCount, freeChunks);
        }
    }

    /**
     * 内存Arena - 管理一块连续的内存区域
     */
    public static class Arena {
        private final int arenaId;
        private final MemorySegment memory;
        private final long size;
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        // 简化的空闲链表管理
        private final ConcurrentLinkedQueue<Long> freeOffsets = new ConcurrentLinkedQueue<>();
        private final AtomicLong nextOffset = new AtomicLong(0);

        // 统计信息
        private final AtomicLong allocatedBytes = new AtomicLong(0);
        private final AtomicLong deallocatedBytes = new AtomicLong(0);
        private final AtomicLong allocatedChunks = new AtomicLong(0);
        private final AtomicLong deallocatedChunks = new AtomicLong(0);

        // VarHandle for efficient memory operations
        private static final VarHandle LONG_HANDLE = MemoryHandles.varHandle(long.class, ByteOrder.nativeOrder());

        public Arena(int arenaId, long size, ResourceScope scope) {
            this.arenaId = arenaId;
            this.size = size;
            this.memory = MemorySegment.allocateNative(size, scope);
        }

        /**
         * 分配内存
         */
        public long allocate(long requestSize) {
            // 对齐到8字节边界
            long alignedSize = (requestSize + 7) & ~7L;

            lock.writeLock().lock();
            try {
                // 首先尝试从空闲链表中分配
                Long freeOffset = freeOffsets.poll();
                if (freeOffset != null) {
                    allocatedBytes.addAndGet(alignedSize);
                    allocatedChunks.incrementAndGet();
                    return freeOffset;
                }

                // 从未使用的内存中分配
                long offset = nextOffset.getAndAdd(alignedSize + 16); // 16字节头部
                if (offset + alignedSize + 16 <= size) {
                    // 写入块头信息
                    LONG_HANDLE.set(memory, offset, alignedSize); // 块大小
                    LONG_HANDLE.set(memory, offset + 8, System.currentTimeMillis()); // 分配时间

                    allocatedBytes.addAndGet(alignedSize);
                    allocatedChunks.incrementAndGet();
                    return offset + 16; // 返回数据部分的偏移
                }

                return -1; // 内存不足
            } finally {
                lock.writeLock().unlock();
            }
        }

        /**
         * 释放内存
         */
        public void deallocate(long offset) {
            if (offset < 16 || offset >= size) {
                return; // 无效偏移
            }

            lock.writeLock().lock();
            try {
                // 读取块大小
                long headerOffset = offset - 16;
                long blockSize = (long) LONG_HANDLE.get(memory, headerOffset);

                // 添加到空闲链表
                freeOffsets.offer(offset);

                deallocatedBytes.addAndGet(blockSize);
                deallocatedChunks.incrementAndGet();
            } finally {
                lock.writeLock().unlock();
            }
        }

        public MemorySegment getMemorySegment(long offset, long size) {
            if (offset < 0 || offset + size > this.size) {
                throw new IndexOutOfBoundsException("Invalid memory range");
            }
            return memory.asSlice(offset, size);
        }

        public ArenaStats getStats() {
            return new ArenaStats(
                    allocatedBytes.get(),
                    deallocatedBytes.get(),
                    allocatedChunks.get(),
                    deallocatedChunks.get()
            );
        }

        public int getArenaId() {
            return arenaId;
        }
    }

    /**
     * 主内存分配器
     */
    public static class JavaJemallocAllocator {
        private final Arena[] arenas;
        private final int numArenas;
        private final AtomicInteger nextArenaIndex = new AtomicInteger(0);
        private final ResourceScope scope;
        private final ThreadLocal<Arena> threadLocalArena = new ThreadLocal<>();

        public JavaJemallocAllocator(int numArenas, long arenaSize) {
            this.numArenas = numArenas;
            this.scope = ResourceScope.newSharedScope();
            this.arenas = new Arena[numArenas];

            // 初始化所有Arena
            for (int i = 0; i < numArenas; i++) {
                this.arenas[i] = new Arena(i, arenaSize, scope);
            }
        }

        public MemoryHandle allocate(long size) {
            // 首先尝试从线程本地Arena分配
            Arena arena = threadLocalArena.get();
            if (arena != null) {
                long offset = arena.allocate(size);
                if (offset != -1) {
                    return new MemoryHandle(arena, offset, size);
                }
            }

            // 轮询其他Arena
            int startIndex = nextArenaIndex.getAndIncrement() % numArenas;
            for (int i = 0; i < numArenas; i++) {
                int arenaIndex = (startIndex + i) % numArenas;
                arena = arenas[arenaIndex];

                long offset = arena.allocate(size);
                if (offset != -1) {
                    threadLocalArena.set(arena);
                    return new MemoryHandle(arena, offset, size);
                }
            }

            throw new OutOfMemoryError("Unable to allocate " + size + " bytes");
        }

        public void deallocate(MemoryHandle handle) {
            handle.getArena().deallocate(handle.getOffset());
        }

        public long getUsedMemory() {
            long total = 0;
            for (Arena arena : arenas) {
                ArenaStats stats = arena.getStats();
                total += stats.allocated - stats.deallocated;
            }
            return total;
        }

        public void close() {
            scope.close();
        }

        public ArenaStats[] getAllArenaStats() {
            ArenaStats[] stats = new ArenaStats[numArenas];
            for (int i = 0; i < numArenas; i++) {
                stats[i] = arenas[i].getStats();
            }
            return stats;
        }
    }

    // ===============================================
    // 2. ByteBuf接口定义
    // ===============================================

    public interface ByteBuf extends Comparable<ByteBuf> {
        // 容量相关
        int capacity();
        ByteBuf capacity(int newCapacity);
        int maxCapacity();
        ByteBufAllocator alloc();

        // 字节序
        ByteOrder order();
        ByteBuf order(ByteOrder endianness);

        // 基本属性
        ByteBuf unwrap();
        boolean isDirect();
        boolean isReadOnly();
        ByteBuf asReadOnly();

        // 索引操作
        int readerIndex();
        ByteBuf readerIndex(int readerIndex);
        int writerIndex();
        ByteBuf writerIndex(int writerIndex);
        ByteBuf setIndex(int readerIndex, int writerIndex);

        // 可读写字节数
        int readableBytes();
        int writableBytes();
        int maxWritableBytes();
        boolean isReadable();
        boolean isReadable(int size);
        boolean isWritable();
        boolean isWritable(int size);

        // 索引管理
        ByteBuf clear();
        ByteBuf markReaderIndex();
        ByteBuf resetReaderIndex();
        ByteBuf markWriterIndex();
        ByteBuf resetWriterIndex();
        ByteBuf discardReadBytes();
        ByteBuf discardSomeReadBytes();
        ByteBuf ensureWritable(int minWritableBytes);
        int ensureWritable(int minWritableBytes, boolean force);

        // 基本读写方法
        byte getByte(int index);
        short getShort(int index);
        int getInt(int index);
        long getLong(int index);
        ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length);

        ByteBuf setByte(int index, int value);
        ByteBuf setShort(int index, int value);
        ByteBuf setInt(int index, int value);
        ByteBuf setLong(int index, long value);
        ByteBuf setBytes(int index, byte[] src, int srcIndex, int length);

        byte readByte();
        short readShort();
        int readInt();
        long readLong();
        ByteBuf readBytes(byte[] dst, int dstIndex, int length);

        ByteBuf writeByte(int value);
        ByteBuf writeShort(int value);
        ByteBuf writeInt(int value);
        ByteBuf writeLong(long value);
        ByteBuf writeBytes(byte[] src, int srcIndex, int length);

        // 其他方法
        ByteBuf copy();
        ByteBuf slice();
        ByteBuf duplicate();
        boolean hasArray();
        byte[] array();
        int arrayOffset();
        boolean hasMemoryAddress();
        long memoryAddress();
        String toString(Charset charset);

        // 引用计数
        int refCnt();
        ByteBuf retain();
        ByteBuf retain(int increment);
        boolean release();
        boolean release(int decrement);
        ByteBuf touch();
        ByteBuf touch(Object hint);
    }

    public interface ByteBufAllocator {
        ByteBuf buffer();
        ByteBuf buffer(int initialCapacity);
        ByteBuf buffer(int initialCapacity, int maxCapacity);
        ByteBuf directBuffer();
        ByteBuf directBuffer(int initialCapacity);
        ByteBuf directBuffer(int initialCapacity, int maxCapacity);
        ByteBuf heapBuffer();
        ByteBuf heapBuffer(int initialCapacity);
        ByteBuf heapBuffer(int initialCapacity, int maxCapacity);
        boolean isDirectBufferPooled();
    }

    // ===============================================
    // 3. 主要的ByteBuf实现
    // ===============================================

    public static class JemallocByteBuf implements ByteBuf {
        private final JemallocByteBufAllocator allocator;
        private final JavaJemallocAllocator memoryAllocator;
        private final boolean isDirect;
        private final int maxCapacity;
        private final AtomicInteger refCnt = new AtomicInteger(1);

        private MemoryHandle handle;
        private MemorySegment segment;
        private int capacity;
        private int readerIndex;
        private int writerIndex;
        private int markedReaderIndex;
        private int markedWriterIndex;
        private ByteOrder order = ByteOrder.BIG_ENDIAN;

        // VarHandle for efficient operations
        private final VarHandle byteHandle = MemoryHandles.varHandle(byte.class, ByteOrder.nativeOrder());
        private final VarHandle shortHandle = MemoryHandles.varHandle(short.class, ByteOrder.nativeOrder());
        private final VarHandle intHandle = MemoryHandles.varHandle(int.class, ByteOrder.nativeOrder());
        private final VarHandle longHandle = MemoryHandles.varHandle(long.class, ByteOrder.nativeOrder());

        public JemallocByteBuf(JemallocByteBufAllocator allocator, JavaJemallocAllocator memoryAllocator,
                               int initialCapacity, int maxCapacity, boolean isDirect) {
            this.allocator = allocator;
            this.memoryAllocator = memoryAllocator;
            this.isDirect = isDirect;
            this.maxCapacity = maxCapacity;
            this.capacity = initialCapacity;

            // 分配内存
            this.handle = memoryAllocator.allocate(initialCapacity);
            this.segment = handle.getMemorySegment();

            allocator.incrementAllocated(initialCapacity);
        }

        @Override
        public int capacity() {
            return capacity;
        }

        @Override
        public ByteBuf capacity(int newCapacity) {
            if (newCapacity < 0 || newCapacity > maxCapacity) {
                throw new IllegalArgumentException("newCapacity: " + newCapacity);
            }

            if (newCapacity == capacity) {
                return this;
            }

            // 重新分配内存
            MemoryHandle newHandle = memoryAllocator.allocate(newCapacity);
            MemorySegment newSegment = newHandle.getMemorySegment();

            // 复制现有数据
            int copyLength = Math.min(capacity, newCapacity);
            for (int i = 0; i < copyLength; i++) {
                byte value = (byte) byteHandle.get(segment, i);
                byteHandle.set(newSegment, i, value);
            }

            // 释放旧内存
            memoryAllocator.deallocate(handle);

            // 更新引用
            this.handle = newHandle;
            this.segment = newSegment;
            this.capacity = newCapacity;

            // 调整索引
            if (readerIndex > newCapacity) {
                readerIndex = newCapacity;
            }
            if (writerIndex > newCapacity) {
                writerIndex = newCapacity;
            }

            return this;
        }

        @Override
        public int maxCapacity() {
            return maxCapacity;
        }

        @Override
        public ByteBufAllocator alloc() {
            return allocator;
        }

        @Override
        public ByteOrder order() {
            return order;
        }

        @Override
        public ByteBuf order(ByteOrder endianness) {
            this.order = endianness;
            return this;
        }

        @Override
        public ByteBuf unwrap() {
            return null;
        }

        @Override
        public boolean isDirect() {
            return isDirect;
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public ByteBuf asReadOnly() {
            return this;
        }

        @Override
        public int readerIndex() {
            return readerIndex;
        }

        @Override
        public ByteBuf readerIndex(int readerIndex) {
            if (readerIndex < 0 || readerIndex > writerIndex) {
                throw new IndexOutOfBoundsException("readerIndex: " + readerIndex);
            }
            this.readerIndex = readerIndex;
            return this;
        }

        @Override
        public int writerIndex() {
            return writerIndex;
        }

        @Override
        public ByteBuf writerIndex(int writerIndex) {
            if (writerIndex < readerIndex || writerIndex > capacity) {
                throw new IndexOutOfBoundsException("writerIndex: " + writerIndex);
            }
            this.writerIndex = writerIndex;
            return this;
        }

        @Override
        public ByteBuf setIndex(int readerIndex, int writerIndex) {
            if (readerIndex < 0 || readerIndex > writerIndex || writerIndex > capacity) {
                throw new IndexOutOfBoundsException();
            }
            this.readerIndex = readerIndex;
            this.writerIndex = writerIndex;
            return this;
        }

        @Override
        public int readableBytes() {
            return writerIndex - readerIndex;
        }

        @Override
        public int writableBytes() {
            return capacity - writerIndex;
        }

        @Override
        public int maxWritableBytes() {
            return maxCapacity - writerIndex;
        }

        @Override
        public boolean isReadable() {
            return writerIndex > readerIndex;
        }

        @Override
        public boolean isReadable(int size) {
            return writerIndex - readerIndex >= size;
        }

        @Override
        public boolean isWritable() {
            return capacity > writerIndex;
        }

        @Override
        public boolean isWritable(int size) {
            return capacity - writerIndex >= size;
        }

        @Override
        public ByteBuf clear() {
            readerIndex = writerIndex = 0;
            return this;
        }

        @Override
        public ByteBuf markReaderIndex() {
            markedReaderIndex = readerIndex;
            return this;
        }

        @Override
        public ByteBuf resetReaderIndex() {
            readerIndex = markedReaderIndex;
            return this;
        }

        @Override
        public ByteBuf markWriterIndex() {
            markedWriterIndex = writerIndex;
            return this;
        }

        @Override
        public ByteBuf resetWriterIndex() {
            writerIndex = markedWriterIndex;
            return this;
        }

        @Override
        public ByteBuf discardReadBytes() {
            if (readerIndex == 0) {
                return this;
            }

            if (readerIndex != writerIndex) {
                // 使用 VarHandle 逐字节移动数据
                int length = writerIndex - readerIndex;
                for (int i = 0; i < length; i++) {
                    byte value = (byte) byteHandle.get(segment, readerIndex + i);
                    byteHandle.set(segment, i, value);
                }
                writerIndex -= readerIndex;
            } else {
                writerIndex = 0;
            }
            readerIndex = 0;
            return this;
        }

        @Override
        public ByteBuf discardSomeReadBytes() {
            return discardReadBytes();
        }

        @Override
        public ByteBuf ensureWritable(int minWritableBytes) {
            if (minWritableBytes <= writableBytes()) {
                return this;
            }

            int newCapacity = Math.min(maxCapacity, writerIndex + minWritableBytes);
            capacity(newCapacity);
            return this;
        }

        @Override
        public int ensureWritable(int minWritableBytes, boolean force) {
            ensureWritable(minWritableBytes);
            return 0;
        }

        // ===============================================
        // 基础读写方法实现
        // ===============================================

        @Override
        public byte getByte(int index) {
            checkIndex(index, 1);
            return (byte) byteHandle.get(segment, index);
        }

        @Override
        public short getShort(int index) {
            checkIndex(index, 2);
            if (order == ByteOrder.BIG_ENDIAN) {
                return (short) ((getByte(index) << 8) | (getByte(index + 1) & 0xFF));
            } else {
                return (short) ((getByte(index + 1) << 8) | (getByte(index) & 0xFF));
            }
        }

        @Override
        public int getInt(int index) {
            checkIndex(index, 4);
            if (order == ByteOrder.BIG_ENDIAN) {
                return (getByte(index) << 24) |
                        ((getByte(index + 1) & 0xFF) << 16) |
                        ((getByte(index + 2) & 0xFF) << 8) |
                        (getByte(index + 3) & 0xFF);
            } else {
                return (getByte(index + 3) << 24) |
                        ((getByte(index + 2) & 0xFF) << 16) |
                        ((getByte(index + 1) & 0xFF) << 8) |
                        (getByte(index) & 0xFF);
            }
        }

        @Override
        public long getLong(int index) {
            checkIndex(index, 8);
            if (order == ByteOrder.BIG_ENDIAN) {
                return ((long) getInt(index) << 32) | (getInt(index + 4) & 0xFFFFFFFFL);
            } else {
                return ((long) getInt(index + 4) << 32) | (getInt(index) & 0xFFFFFFFFL);
            }
        }

        @Override
        public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
            checkIndex(index, length);
            // 使用 VarHandle 逐字节复制
            for (int i = 0; i < length; i++) {
                dst[dstIndex + i] = (byte) byteHandle.get(segment, index + i);
            }
            return this;
        }

        @Override
        public ByteBuf setByte(int index, int value) {
            checkIndex(index, 1);
            byteHandle.set(segment, index, (byte) value);
            return this;
        }

        @Override
        public ByteBuf setShort(int index, int value) {
            checkIndex(index, 2);
            if (order == ByteOrder.BIG_ENDIAN) {
                setByte(index, (byte) (value >>> 8));
                setByte(index + 1, (byte) value);
            } else {
                setByte(index, (byte) value);
                setByte(index + 1, (byte) (value >>> 8));
            }
            return this;
        }

        @Override
        public ByteBuf setInt(int index, int value) {
            checkIndex(index, 4);
            if (order == ByteOrder.BIG_ENDIAN) {
                setByte(index, (byte) (value >>> 24));
                setByte(index + 1, (byte) (value >>> 16));
                setByte(index + 2, (byte) (value >>> 8));
                setByte(index + 3, (byte) value);
            } else {
                setByte(index, (byte) value);
                setByte(index + 1, (byte) (value >>> 8));
                setByte(index + 2, (byte) (value >>> 16));
                setByte(index + 3, (byte) (value >>> 24));
            }
            return this;
        }

        @Override
        public ByteBuf setLong(int index, long value) {
            checkIndex(index, 8);
            if (order == ByteOrder.BIG_ENDIAN) {
                setInt(index, (int) (value >>> 32));
                setInt(index + 4, (int) value);
            } else {
                setInt(index, (int) value);
                setInt(index + 4, (int) (value >>> 32));
            }
            return this;
        }

        @Override
        public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
            checkIndex(index, length);
            // 使用 VarHandle 逐字节复制
            for (int i = 0; i < length; i++) {
                byteHandle.set(segment, index + i, src[srcIndex + i]);
            }
            return this;
        }

        // Read方法实现
        @Override
        public byte readByte() {
            checkReadableBytes(1);
            byte value = getByte(readerIndex);
            readerIndex++;
            return value;
        }

        @Override
        public short readShort() {
            checkReadableBytes(2);
            short value = getShort(readerIndex);
            readerIndex += 2;
            return value;
        }

        @Override
        public int readInt() {
            checkReadableBytes(4);
            int value = getInt(readerIndex);
            readerIndex += 4;
            return value;
        }

        @Override
        public long readLong() {
            checkReadableBytes(8);
            long value = getLong(readerIndex);
            readerIndex += 8;
            return value;
        }

        @Override
        public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
            checkReadableBytes(length);
            getBytes(readerIndex, dst, dstIndex, length);
            readerIndex += length;
            return this;
        }

        // Write方法实现
        @Override
        public ByteBuf writeByte(int value) {
            ensureWritable(1);
            setByte(writerIndex, value);
            writerIndex++;
            return this;
        }

        @Override
        public ByteBuf writeShort(int value) {
            ensureWritable(2);
            setShort(writerIndex, value);
            writerIndex += 2;
            return this;
        }

        @Override
        public ByteBuf writeInt(int value) {
            ensureWritable(4);
            setInt(writerIndex, value);
            writerIndex += 4;
            return this;
        }

        @Override
        public ByteBuf writeLong(long value) {
            ensureWritable(8);
            setLong(writerIndex, value);
            writerIndex += 8;
            return this;
        }

        @Override
        public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
            ensureWritable(length);
            setBytes(writerIndex, src, srcIndex, length);
            writerIndex += length;
            return this;
        }

        // 工具方法
        private void checkIndex(int index, int length) {
            if (index < 0 || index + length > capacity) {
                throw new IndexOutOfBoundsException("index: " + index + ", length: " + length + ", capacity: " + capacity);
            }
        }

        private void checkReadableBytes(int minimumReadableBytes) {
            if (readableBytes() < minimumReadableBytes) {
                throw new IndexOutOfBoundsException("Not enough readable bytes");
            }
        }

        // ===============================================
        // 其他必需方法的实现
        // ===============================================

        @Override
        public ByteBuf copy() {
            ByteBuf copy = allocator.buffer(capacity);
            // 手动复制数据
            for (int i = 0; i < capacity; i++) {
                copy.setByte(i, getByte(i));
            }
            copy.writerIndex(writerIndex);
            copy.readerIndex(readerIndex);
            return copy;
        }

        @Override
        public ByteBuf slice() {
            return copy();
        }

        @Override
        public ByteBuf duplicate() {
            return copy();
        }

        @Override
        public boolean hasArray() {
            return false;
        }

        @Override
        public byte[] array() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int arrayOffset() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasMemoryAddress() {
            return true;
        }

        @Override
        public long memoryAddress() {
            return segment.address().toRawLongValue();
        }

        @Override
        public String toString(Charset charset) {
            byte[] bytes = new byte[readableBytes()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = getByte(readerIndex + i);
            }
            return new String(bytes, charset);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public int compareTo(ByteBuf buffer) {
            return Integer.compare(this.hashCode(), buffer.hashCode());
        }

        @Override
        public String toString() {
            return String.format("JemallocByteBuf(ridx=%d, widx=%d, cap=%d, refCnt=%d)",
                    readerIndex, writerIndex, capacity, refCnt.get());
        }

        // ===============================================
        // 引用计数实现
        // ===============================================

        @Override
        public int refCnt() {
            return refCnt.get();
        }

        @Override
        public ByteBuf retain() {
            return retain(1);
        }

        @Override
        public ByteBuf retain(int increment) {
            if (increment <= 0) {
                throw new IllegalArgumentException("increment: " + increment);
            }

            for (;;) {
                int refCnt = this.refCnt.get();
                if (refCnt == 0) {
                    throw new IllegalStateException("ByteBuf is already released");
                }
                if (this.refCnt.compareAndSet(refCnt, refCnt + increment)) {
                    break;
                }
            }
            return this;
        }

        @Override
        public ByteBuf touch() {
            return this;
        }

        @Override
        public ByteBuf touch(Object hint) {
            return this;
        }

        @Override
        public boolean release() {
            return release(1);
        }

        @Override
        public boolean release(int decrement) {
            if (decrement <= 0) {
                throw new IllegalArgumentException("decrement: " + decrement);
            }

            for (;;) {
                int refCnt = this.refCnt.get();
                if (refCnt < decrement) {
                    throw new IllegalStateException("refCnt < decrement");
                }

                if (this.refCnt.compareAndSet(refCnt, refCnt - decrement)) {
                    if (refCnt == decrement) {
                        // 释放内存
                        deallocate();
                        return true;
                    }
                    return false;
                }
            }
        }

        private void deallocate() {
            if (handle != null) {
                allocator.incrementDeallocated(capacity);
                memoryAllocator.deallocate(handle);
                handle = null;
                segment = null;
            }
        }
    }

    // ===============================================
    // 4. ByteBufAllocator实现
    // ===============================================

    public static class JemallocByteBufAllocator implements ByteBufAllocator {
        private final JavaJemallocAllocator allocator;
        private final boolean preferDirect;
        private final AtomicLong allocatedMemory = new AtomicLong();
        private final AtomicLong deallocatedMemory = new AtomicLong();

        public JemallocByteBufAllocator(boolean preferDirect, int nArenas, long arenaSize) {
            this.preferDirect = preferDirect;
            this.allocator = new JavaJemallocAllocator(nArenas, arenaSize);
        }

        @Override
        public boolean isDirectBufferPooled() {
            return true;
        }

        @Override
        public ByteBuf buffer() {
            return preferDirect ? directBuffer() : heapBuffer();
        }

        @Override
        public ByteBuf buffer(int initialCapacity) {
            return preferDirect ? directBuffer(initialCapacity) : heapBuffer(initialCapacity);
        }

        @Override
        public ByteBuf buffer(int initialCapacity, int maxCapacity) {
            return preferDirect ? directBuffer(initialCapacity, maxCapacity) :
                    heapBuffer(initialCapacity, maxCapacity);
        }

        @Override
        public ByteBuf heapBuffer() {
            return heapBuffer(256, Integer.MAX_VALUE);
        }

        @Override
        public ByteBuf heapBuffer(int initialCapacity) {
            return heapBuffer(initialCapacity, Integer.MAX_VALUE);
        }

        @Override
        public ByteBuf heapBuffer(int initialCapacity, int maxCapacity) {
            return new JemallocByteBuf(this, allocator, initialCapacity, maxCapacity, false);
        }

        @Override
        public ByteBuf directBuffer() {
            return directBuffer(256, Integer.MAX_VALUE);
        }

        @Override
        public ByteBuf directBuffer(int initialCapacity) {
            return directBuffer(initialCapacity, Integer.MAX_VALUE);
        }

        @Override
        public ByteBuf directBuffer(int initialCapacity, int maxCapacity) {
            return new JemallocByteBuf(this, allocator, initialCapacity, maxCapacity, true);
        }

        void incrementAllocated(long size) {
            allocatedMemory.addAndGet(size);
        }

        void incrementDeallocated(long size) {
            deallocatedMemory.addAndGet(size);
        }

        public long usedMemory() {
            return allocatedMemory.get() - deallocatedMemory.get();
        }

        public ArenaStats[] getArenaStats() {
            return allocator.getAllArenaStats();
        }

        public void close() {
            allocator.close();
        }

        @Override
        public String toString() {
            return String.format("JemallocByteBufAllocator(allocated=%d, deallocated=%d, used=%d)",
                    allocatedMemory.get(), deallocatedMemory.get(), usedMemory());
        }
    }

    // ===============================================
    // 5. 使用示例和测试
    // ===============================================

    public static void main(String[] args) {
        System.out.println("=== 完整可编译的Netty兼容JavaJemalloc演示 ===");

        // 创建分配器
        JemallocByteBufAllocator allocator = new JemallocByteBufAllocator(true, 4, 64 * 1024 * 1024);

        try {
            // 基础功能测试
            testBasicOperations(allocator);

            // 性能测试
            testPerformance(allocator);

            // 引用计数测试
            testReferenceCount(allocator);

            // 内存统计
            printMemoryStats(allocator);

        } finally {
            allocator.close();
        }

        System.out.println("=== 所有测试完成 ===");
    }

    private static void testBasicOperations(JemallocByteBufAllocator allocator) {
        System.out.println("\n--- 基础操作测试 ---");

        ByteBuf buf = allocator.directBuffer(1024);
        System.out.println("创建ByteBuf: " + buf);

        // 写入数据
        buf.writeInt(42);
        buf.writeLong(123456789L);
        byte[] helloBytes = "Hello, JavaJemalloc!".getBytes(StandardCharsets.UTF_8);
        buf.writeBytes(helloBytes, 0, helloBytes.length);

        System.out.println("写入后 - 可读字节: " + buf.readableBytes());
        System.out.println("写入后 - 可写字节: " + buf.writableBytes());

        // 读取数据
        buf.readerIndex(0);
        int intValue = buf.readInt();
        long longValue = buf.readLong();
        byte[] stringBytes = new byte[helloBytes.length];
        buf.readBytes(stringBytes, 0, stringBytes.length);

        System.out.printf("读取的数据: int=%d, long=%d, string=%s%n",
                intValue, longValue, new String(stringBytes));

        buf.release();
        System.out.println("基础操作测试完成");
    }

    private static void testPerformance(JemallocByteBufAllocator allocator) {
        System.out.println("\n--- 性能测试 ---");

        int iterations = 10000;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            ByteBuf buf = allocator.directBuffer(1024);

            // 写入测试数据
            buf.writeInt(i);
            buf.writeLong(System.nanoTime());
            byte[] testData = "Performance test data".getBytes(StandardCharsets.UTF_8);
            buf.writeBytes(testData, 0, testData.length);

            // 读取测试数据
            buf.readerIndex(0);
            buf.readInt();
            buf.readLong();
            byte[] temp = new byte[testData.length];
            buf.readBytes(temp, 0, temp.length);

            buf.release();
        }

        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;

        System.out.printf("性能测试完成: %d 次操作, 总时间: %.2f ms%n",
                iterations, totalTime / 1_000_000.0);
        System.out.printf("平均每次操作: %.0f ns%n", (double) totalTime / iterations);
    }

    private static void testReferenceCount(JemallocByteBufAllocator allocator) {
        System.out.println("\n--- 引用计数测试 ---");

        ByteBuf buf = allocator.directBuffer(512);
        System.out.println("初始引用计数: " + buf.refCnt());

        buf.retain();
        System.out.println("retain后引用计数: " + buf.refCnt());

        buf.retain(2);
        System.out.println("retain(2)后引用计数: " + buf.refCnt());

        buf.release();
        System.out.println("release后引用计数: " + buf.refCnt());

        buf.release(3);
        System.out.println("引用计数测试完成");
    }

    private static void printMemoryStats(JemallocByteBufAllocator allocator) {
        System.out.println("\n--- 内存统计 ---");
        System.out.println("分配器统计: " + allocator);

        ArenaStats[] arenaStats = allocator.getArenaStats();
        for (int i = 0; i < arenaStats.length; i++) {
            System.out.println("Arena " + i + ": " + arenaStats[i]);
        }
    }
}
