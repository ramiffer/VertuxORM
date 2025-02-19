package org.project.cache.model;

public class CacheKey {

    public CacheType type;
    public String key;

    public CacheKey(CacheType type, String key) {
        this.type = type;
        this.key = key;
    }

    @Override
    public String toString() {
        return type + ":" + key;
    }
}
