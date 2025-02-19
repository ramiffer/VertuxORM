package org.project.cache.interfaces;

import java.util.Optional;

public interface EntityCache {

    <T> Optional<T> getEntity(Class<T> entityClass, Object id);
    <T> void putEntity(Class<T> entityClass, Object id, T entity);
    <T> void invalidateEntity(Class<T> entityClass, Object id);
    void invalidateEntityType(Class<?> entityClass);

}
