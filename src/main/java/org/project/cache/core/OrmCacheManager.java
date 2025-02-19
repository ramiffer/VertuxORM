package org.project.cache.core;

import org.project.cache.interfaces.EntityCache;
import org.project.cache.interfaces.MetadataCache;
import org.project.cache.interfaces.QueryCache;
import org.project.cache.model.CacheType;
import org.project.cache.model.EntityMetadata;
import org.project.cache.util.SmartCache;
import org.project.cache.util.TypedCacheKey;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OrmCacheManager implements EntityCache, QueryCache, MetadataCache {

    private final SmartCache<TypedCacheKey<?>, Object> cache;
    private final ConcurrentHashMap<String, Set<String>> tableQueriesMap;

    public OrmCacheManager(Duration defaultTtl) {
        this.cache = new SmartCache<>(1000, defaultTtl);
        this.tableQueriesMap = new ConcurrentHashMap<>();
    }


    // EntityCache implementacion

    @Override
    public <T> Optional<T> getEntity(Class<T> entityClass, Object id) {
        TypedCacheKey<T> key = TypedCacheKey.createEntityKey(entityClass, id);
        return Optional.ofNullable(entityClass.cast(cache.get(key, k -> null)));
    }

    @Override
    public <T> void putEntity(Class<? extends T> entityClass, Object id, T entity) {
        TypedCacheKey<? extends T> key = TypedCacheKey.createEntityKey(entityClass, id);
        cache.get(key, k -> entity);
    }

    @Override
    public <T> void invalidateEntity(Class<T> entityClass, Object id) {
        TypedCacheKey<T> key = TypedCacheKey.createEntityKey(entityClass, id);
        cache.invalidate(key);
    }

    @Override
    public void invalidateEntityType(Class<?> entityClass) {
        this.invalidateByPrefix(CacheType.ENTITY + ":" + entityClass.getName());
    }

    // MetadataCache implementation

    @Override
    public Optional<EntityMetadata> getMetadata(Class<?> entityClass) {
        TypedCacheKey<EntityMetadata> key = TypedCacheKey.createMetadataKey(entityClass);
        return Optional.ofNullable((EntityMetadata) cache.get(key, k -> null));
    }

    @Override
    public void putMetadata(Class<?> entityClass, EntityMetadata metadata) {
        TypedCacheKey<EntityMetadata> key = TypedCacheKey.createMetadataKey(entityClass);
        cache.get(key, k -> metadata);
    }

    @Override
    public void invalidateMetadata(Class<?> entityClass) {
        TypedCacheKey<EntityMetadata> key = TypedCacheKey.createMetadataKey(entityClass);
        cache.invalidate(key);
    }

    // QueryCache implementation

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<List<T>> getQueryResult(String query, Class<T> entityClass) {
        TypedCacheKey<List<T>> key = TypedCacheKey.createQueryKey(query, entityClass);
        return Optional.ofNullable((List<T>) cache.get(key, k -> null));
    }

    @Override
    public <T> void putQueryResult(String query, Class<T> entityClass, List<T> results) {
        TypedCacheKey<List<T>> key = TypedCacheKey.createQueryKey(query, entityClass);
        cache.get(key, k -> results);

        //Registrar la query para la tabla correspondiente
        String tableName = entityClass.getSimpleName().toLowerCase();
        tableQueriesMap.computeIfAbsent(tableName, k -> ConcurrentHashMap.newKeySet()).add(query);
    }

    @Override
    public void invalidateQuery(String query) {
        tableQueriesMap.values().stream()
                .filter(queries -> queries.contains(query))
                .forEach(queries -> queries.remove(query));
    }

    @Override
    public void invalidateQueriesForTable(String tableName) {
        Set<String> queries = tableQueriesMap.get(tableName);
        if (queries != null) {
            queries.forEach(this::invalidateQuery);
            queries.clear();
        }
    }

    // mEtodos utiles para mejorar el manejo

    public void clearCache() {
        cache.invalidateAll();
        tableQueriesMap.clear();
    }

    /**
     * Establece un TTL personalizado para una entidad especifica
     */
    public <T> void setEntityTTL(Class<T> entityClass,Object id, Duration ttl) {
        TypedCacheKey<T> key = TypedCacheKey.createEntityKey(entityClass, id);
        cache.setTTL(key, ttl);
    }

    /**
     * Establece un TTL personalizado para un tipo de entidad
     */
    public void setEntityTypeTTL(Class<?> entityClass, Duration ttl) {
        String prefix = CacheType.ENTITY + ":" + entityClass.getName();
        cache.getKeys().stream()
                .filter(key -> key.toString().startsWith(prefix))
                .forEach(key -> cache.setTTL(key, ttl));
    }

    /**
     * Establece un TTL personalizado para los resultados de una consulta
     */
    public <T> void setQueryTTL(String query, Class<T> entityClass, Duration ttl) {
        TypedCacheKey<List<T>> key = TypedCacheKey.createQueryKey(query, entityClass);
        cache.setTTL(key, ttl);
    }

    //Utility

    private void invalidateByPrefix(String prefix) {
        cache.getKeys().stream()
                .filter(key -> key.toString().startsWith(prefix))
                .forEach(cache::invalidate);
    }

    public boolean isEntityCached(Class<?> entityClass, Object id) {
        TypedCacheKey<?> key = TypedCacheKey.createEntityKey(entityClass, id);
        return cache.get(key, k -> null) != null;
    }

    public Set<String> getQueriesForTable(String tableName) {
        return tableQueriesMap.getOrDefault(tableName, ConcurrentHashMap.newKeySet());
    }

    public SmartCache.CacheStats getCacheStats() {
        return cache.getStats();
    }

}
