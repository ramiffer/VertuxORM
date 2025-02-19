package org.project.cache.core;

import org.project.cache.interfaces.EntityCache;
import org.project.cache.interfaces.MetadataCache;
import org.project.cache.interfaces.QueryCache;
import org.project.cache.model.CacheKey;
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

    private final SmartCache<CacheKey, Object > cache;

    private final ConcurrentHashMap<String, Set<String>> tableQueriesMap;

    public OrmCacheManager(Duration defaultTtl) {
        this.cache = new SmartCache<>(1000, defaultTtl);
        this.tableQueriesMap = new ConcurrentHashMap<>();
    }


    // EntityCache implementacion

    @Override
    public <T> Optional<T> getEntity(Class<T> entityClass, Object id) {
        TypedCacheKey<T> key = new TypedCacheKey<>(
               CacheType.ENTITY,
               entityClass.getName() + ":" + id,
               entityClass
        );
        return Optional.ofNullable(entityClass.cast(cache.get(key, k -> null)));
    }

    @Override
    public <T> void putEntity(Class<T> entityClass, Object id, T entity) {
        TypedCacheKey<T> key = new TypedCacheKey<>(
                CacheType.ENTITY,
                entityClass.getName() + ":" + id,
                entityClass
        );
        cache.get(key, k -> entity);
    }

    @Override
    public <T> void invalidateEntity(Class<T> entityClass, Object id) {
        CacheKey key = new CacheKey(CacheType.ENTITY, entityClass.getName() + ":" + id);
        cache.invalidate(key);
    }

    @Override
    public void invalidateEntityType(Class<?> entityClass) {
        this.invalidateByPrefix(CacheType.ENTITY + ":" + entityClass.getName());
    }

    // MetadataCache implementation

    @Override
    public Optional<EntityMetadata> getMetadata(Class<?> entityClass) {
        TypedCacheKey<EntityMetadata> key = new TypedCacheKey<>(
                CacheType.METADATA,
                entityClass.getName(),
                EntityMetadata.class
        );
        return Optional.ofNullable((EntityMetadata) cache.get(key, k -> null));
    }

    @Override
    public void putMetadata(Class<?> entityClass, EntityMetadata metadata) {
        CacheKey key = new CacheKey(CacheType.METADATA, entityClass.getName());
        cache.get(key, k -> metadata);
    }

    @Override
    public void invalidateMetadata(Class<?> entityClass) {
        CacheKey key = new CacheKey(CacheType.METADATA, entityClass.getName());
        cache.invalidate(key);
    }

    // QueryCache implementation

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<List<T>> getQueryResult(String query, Class<T> entityClass) {
        TypedCacheKey<List<T>> key = new TypedCacheKey<>(
                CacheType.QUERY,
                entityClass.getName() + ":" + query,
                (Class<List<T>>) (Class<?>) List.class
        );
        return Optional.ofNullable((List<T>) cache.get(key, k -> null));
    }

    @Override
    public <T> void putQueryResult(String query, Class<T> entityClass, List<T> results) {
        CacheKey key = new CacheKey(CacheType.QUERY, entityClass.getName() + ":" + query);
        cache.get(key, k -> results);

        //Registrar la query para la tabla correspondiente
        String tableName = entityClass.getSimpleName().toLowerCase();
        tableQueriesMap.computeIfAbsent(tableName, k -> ConcurrentHashMap.newKeySet()).add(query);
    }

    @Override
    public void invalidateQuery(String query) {
        //Invalidar una query especifica para todas las clases que la usan
        cache.invalidateAll(); //Es una implementacion mas sofisticada, solo invalidariamos la query especifica
    }

    @Override
    public void invalidateQueriesForTable(String tableName) {
        Set<String> queries = tableQueriesMap.get(tableName);
        if (queries != null) {
            queries.forEach(this::invalidateQuery);
        }
    }

    //Utility
    private void invalidateByPrefix(String prefix) {
        cache.invalidateAll();
    }
}
