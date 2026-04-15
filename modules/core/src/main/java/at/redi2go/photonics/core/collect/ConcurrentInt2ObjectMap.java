package at.redi2go.photonics.core.collect;

import com.trivago.fastutilconcurrentwrapper.PrimitiveKeyMap;
import com.trivago.fastutilconcurrentwrapper.map.PrimitiveConcurrentMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentInt2ObjectMap<V> extends PrimitiveConcurrentMap implements IntObjectMap<V> {
    private final WrapperMap<V>[] maps;

    @SuppressWarnings("unchecked")
    public ConcurrentInt2ObjectMap(int numBuckets) {
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
    public boolean containsKey(int key) {
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
    public V get(int key) {
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
    public V put(int key, V value) {
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
    public V remove(int key) {
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
    public boolean remove(int key, V value) {
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
    public V computeIfAbsent(int key, Int2ObjectFunction<V> mappingFunction) {
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
    public V computeIfPresent(int key, BiFunction<Integer, V, V> mappingFunction) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].computeIfPresent(key, mappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    private static class WrapperMap<V> extends Int2ObjectOpenHashMap<V> implements PrimitiveKeyMap {

    }
}
