package org.project.cache.model;

import java.time.Duration;

public enum CacheType {

    METADATA(Duration.ofMinutes(30)),
    QUERY(Duration.ofMinutes(5)),
    ENTITY(Duration.ofHours(12));

    private final Duration defaultTtl;

    CacheType(Duration defaultTtl) {
        this.defaultTtl = defaultTtl;
    }

    public Duration getDefaultTtl() {
        return defaultTtl;
    }
}
