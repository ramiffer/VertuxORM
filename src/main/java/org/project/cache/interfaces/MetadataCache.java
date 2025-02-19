package org.project.cache.interfaces;

import org.project.cache.model.EntityMetadata;

import java.util.Optional;

public interface MetadataCache {

    Optional<EntityMetadata> getMetadata(Class<?> entityClass);
    void putMetadata(Class<?> entityClass, EntityMetadata metadata);
    void invalidateMetadata(Class<?> entityClass);

}
