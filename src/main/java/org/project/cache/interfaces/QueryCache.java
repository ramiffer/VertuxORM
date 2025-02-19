package org.project.cache.interfaces;

import java.util.List;
import java.util.Optional;

public interface QueryCache {

    <T> Optional<List<T>> getQueryResult(String query, Class<T> entityClass);
    <T> void putQueryResult(String query, Class<T> entityClass, List<T> results);
    void invalidateQuery(String query);
    void invalidateQueriesForTable(String tableName);

}
