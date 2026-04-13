package at.redi2go.photonics.core.collect;

import com.trivago.fastutilconcurrentwrapper.PrimitiveKeyMap;
import com.trivago.fastutilconcurrentwrapper.map.PrimitiveConcurrentMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentLong2ObjectMap<V> extends PrimitiveConcurrentMap implements LongObjectMap<V> {
    private final WrapperMap<V>[] maps;

    @SuppressWarnings("unchecked")
    public ConcurrentLong2ObjectMap(int numBuckets) {
        super(numBuckets);

        this.maps = new WrapperMap[numBuckets];
        for (int i = 0; i < numBuckets; i++) {
            maps[i] = new WrapperMap<>();
        }
    }

    @Override
    public int size() {
        return super.size(maps);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty(maps);
    }

    @Override
    public boolean containsKey(long key) {
        int bucket = getBucket(key);

        Lock readLock = locks[bucket].readLock();
        readLock.lock();
        try {
            return maps[bucket].containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public V get(long key) {
        int bucket = getBucket(key);

        V result;

        Lock readLock = locks[bucket].readLock();
        readLock.lock();
        try {
            result = maps[bucket].get(key);
        } finally {
            readLock.unlock();
        }

        return result;
    }

    @Override
    public V put(long key, V value) {
        int bucket = getBucket(key);

        V result;

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            result = maps[bucket].put(key, value);
        } finally {
            writeLock.unlock();
        }

        return result;
    }

    @Override
    public V remove(long key) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean remove(long key, V value) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].remove(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V computeIfAbsent(long key, Long2ObjectFunction<V> mappingFunction) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].computeIfAbsent(key, mappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V computeIfPresent(long key, BiFunction<Long, V, V> mappingFunction) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].computeIfPresent(key, mappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    private static class WrapperMap<V> extends Long2ObjectOpenHashMap<V> implements PrimitiveKeyMap {

    }
}
