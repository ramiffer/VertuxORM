package org.project.cache.util;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SmartCache<K, V> {

    private final Map<K, CacheEntry<V>> firstLevelCache;
    private final Map<K, CacheEntry<V>> secondLevelCache;
    private final Duration ttl;

    private static class CacheEntry<V> {

        private final V value;
        private final Instant expirationTime;
        private int hitCount;

        public CacheEntry(V value, Duration ttl) {
            this.value = value;
            this.expirationTime = Instant.now().plus(ttl);
            this.hitCount = 0;
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expirationTime);
        }

        public void incrementHitCount() {
            hitCount++;
        }
    }

    public SmartCache(int maxSize, Duration ttl) {
        this.ttl = ttl;

        this.firstLevelCache = new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                //Eviccion: mover a segundo nivel si esta lleno
                if (size() > maxSize) {
                    secondLevelCache.put(eldest.getKey(), eldest.getValue());
                    return true;
                }
                return false;
            }
        };

        this.secondLevelCache = new ConcurrentHashMap<>();

    }

    public Set<K> getKeys() {
        Set<K> allKeys = new HashSet<>();
        allKeys.addAll(firstLevelCache.keySet());
        allKeys.addAll(secondLevelCache.keySet());
        return allKeys;
    }

    public V get(K key, Function<K, V> loader) {
        CacheEntry<V> entry = firstLevelCache.get(key);

        if (entry != null) {
            if (!entry.isExpired()) {
                entry.incrementHitCount();
                return entry.value;
            } else firstLevelCache.remove(key);
        }

        entry = secondLevelCache.get(key);

        if (entry != null) {
            if (!entry.isExpired()) {
                if (entry.hitCount > 5) {
                    firstLevelCache.put(key, entry);
                    secondLevelCache.remove(key);
                }
                entry.incrementHitCount();
                return entry.value;
            } else secondLevelCache.remove(key);
        }

        //Cargar valor si no esta en ningun nivel

        V value = loader.apply(key);
        CacheEntry<V> newEntry = new CacheEntry<>(value, ttl);
        firstLevelCache.put(key, newEntry);
        return value;
    }

    public void setTTL(K key, Duration newTtl) {
        CacheEntry<V> entry = firstLevelCache.get(key);
        if (entry != null) {
            V value = entry.value;
            firstLevelCache.put(key, new CacheEntry<>(value, newTtl));
            return;
        }

        entry = secondLevelCache.get(key);
        if (entry != null) {
            V value = entry.value;
            secondLevelCache.put(key, new CacheEntry<>(value, newTtl));
        }
    }

    public void cleanExpiredEntries() {
        firstLevelCache.entrySet().removeIf(e -> e.getValue().isExpired());
        secondLevelCache.entrySet().removeIf(e -> e.getValue().isExpired());
    }


    public void invalidate(K key) {
        firstLevelCache.remove(key);
        secondLevelCache.remove(key);
    }

    public void invalidateAll() {
        firstLevelCache.clear();
        secondLevelCache.clear();
    }

    public CacheStats getStats() {
        return new CacheStats(
                firstLevelCache.size(),
                secondLevelCache.size(),
                firstLevelCache.values().stream()
                        .mapToInt(entry -> entry.hitCount).sum(),
                secondLevelCache.values().stream()
                        .mapToInt(entry -> entry.hitCount).sum()
        );
    }

    public record CacheStats(int firstLevelSize, int secondLevelSize, int firstLevelHits, int secondLevelHits) {}


}
