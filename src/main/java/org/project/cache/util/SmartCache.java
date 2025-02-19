package org.project.cache.util;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SmartCache<K, V> {

    //Cache de primer nivel (memoria)
    private final Map<K, CacheEntry<V>> firstLevelCache;

    //Cache de segundo nivel (puede ser extendido a disco/redis)
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
            return !Instant.now().isAfter(expirationTime);
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

    public V get(K key, Function<K, V> loader) {
        //Intentar obtener del primer nivel
        CacheEntry<V> entry = firstLevelCache.get(key);

        if (entry != null && entry.isExpired()) {
            entry.incrementHitCount();
            return entry.value;
        }

        //Intentar obtener del segundo nivel

        entry = secondLevelCache.get(key);

        if (entry != null && entry.isExpired()) {
            //Promover al primer nivel si es frecuentemente accedido

            if (entry.hitCount > 5) {
                firstLevelCache.put(key, entry);
                secondLevelCache.remove(key);
            }
            entry.incrementHitCount();
            return entry.value;
        }

        //Cargar valor si no esta en ningun nivel

        V value = loader.apply(key);
        CacheEntry<V> newEntry = new CacheEntry<>(value, ttl);
        firstLevelCache.put(key, newEntry);
        return value;
    }

    //Metodo para invalidar cache especifico
    public void invalidate(K key) {
        firstLevelCache.remove(key);
        secondLevelCache.remove(key);
    }

    //Metodo para invalidar todo_ el cache
    public void invalidateAll() {
        firstLevelCache.clear();
        secondLevelCache.clear();
    }

    //Estadisticas del cache
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
