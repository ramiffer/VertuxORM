package org.project.cache.util;

import org.project.cache.model.CacheType;
import org.project.cache.model.EntityMetadata;

import java.util.List;
import java.util.Objects;

public class TypedCacheKey<T>  {

    private final CacheType type;
    private final String key;
    private final Class<T> valueType;

    public TypedCacheKey(CacheType type, String key, Class<T> valueType) {
        this.type = type;
        this.key = key;
        this.valueType = valueType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypedCacheKey<?> that)) return false;
        return type == that.type
                && key.equals(that.key) &&
                valueType.equals(that.valueType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, key, valueType);
    }

    public static <T> TypedCacheKey<T> createEntityKey(Class<T> entityClass, Object id) {
        return new TypedCacheKey<>(
                CacheType.ENTITY,
                entityClass.getName() + ":" + id,
                entityClass
        );
    }

    public static TypedCacheKey<EntityMetadata> createMetadataKey(Class<?> entityClass) {
        return new TypedCacheKey<>(
                CacheType.METADATA,
                entityClass.getName(),
                EntityMetadata.class
        );
    }

    @SuppressWarnings("unchecked")
    public static <T> TypedCacheKey<List<T>> createQueryKey(String query, Class<?> entityClass) {
        return new TypedCacheKey<>(
                CacheType.QUERY,
                entityClass.getName() + ":" + query,
                (Class<List<T>>) (Class<?>) List.class
        );
    }

}
