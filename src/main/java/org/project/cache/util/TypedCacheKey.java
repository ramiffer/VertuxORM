package org.project.cache.util;

import org.project.cache.model.CacheKey;
import org.project.cache.model.CacheType;

import java.util.Objects;

public class TypedCacheKey<T> extends CacheKey {

    private final CacheType type;
    private final String key;
    private final Class<T> valueType;

    public TypedCacheKey(CacheType type, String key, Class<T> valueType) {
        super(type, key);
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

}
