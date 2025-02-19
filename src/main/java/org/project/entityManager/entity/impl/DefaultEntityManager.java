package org.project.entityManager.entity.impl;

import org.project.cache.core.OrmCacheManager;
import org.project.cache.model.EntityMetadata;
import org.project.entityManager.configuration.EntityManagerConfig;
import org.project.entityManager.entity.ConnectionPool;
import org.project.entityManager.entity.EntityManager;
import org.project.entityManager.entity.MetadataManager;
import org.project.entityManager.entity.QueryExecutor;
import org.project.entityManager.transaction.Transaction;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultEntityManager implements EntityManager {

    private final ConnectionPool connectionPool;
    private final MetadataManager metadataManager;
    private final QueryExecutor queryExecutor;
    private final OrmCacheManager cacheManager;
    private Transaction currentTransaction;
    private boolean isOpen;

    public DefaultEntityManager(EntityManagerConfig config) {
        this.connectionPool = new ConnectionPool(config.getDataSource());
        this.metadataManager = new MetadataManager();
        this.queryExecutor = new QueryExecutor();
        this.cacheManager = new OrmCacheManager(config.getDefaultCacheTtl());
        this.isOpen = true;
    }



    @Override
    public <T> T find(Class<T> entityClass, Object id) {
        this.checkOpen();

        //Intentar obtener del cache
        Optional<T> cached = cacheManager.getEntity(entityClass, id);
        if (cached.isPresent()) return cached.get();

        //Obtener metadata de la entidad
        EntityMetadata metadata = metadataManager.getEntityMetadata(entityClass);
        String tableName = metadata.getTableName();
        String idColumn = metadata.getIdColumn();

        //Construir y ejecutar query
        String sql = String.format("SELECT * FROM %s WHERE %s = ?", tableName, idColumn);
        List<T> results = new ArrayList<>(); // ACA VA EL QUERYEXECUTOR

        if (!results.isEmpty()) {
            T entity = results.get(0);
            cacheManager.putEntity(entityClass, id, entity);
            return entity;
        }

        return null;
    }

    @Override
    public <T> void persist(T entity) {
        this.checkOpen();
        EntityMetadata metadata = metadataManager.getEntityMetadata(entity.getClass());

        //Extraer valores de campos
        Map<String, Object> values = metadata.extractValues(entity);
        String tableName = metadata.getTableName();

        //Construir query de insercion
        String columns = String.join(", ", values.keySet());
        String placeHolders = String.join(", ", Collections.nCopies(values.size(), "?"));
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeHolders);

        //Ejecutar insercion
        Object id = new Object(); // ACA VA EL QUERYEXECUTOR
        metadata.setId(entity, id);

        //Invalidar queries relacionadas
        cacheManager.invalidateQueriesForTable(tableName);
    }

    @Override
    public <T> T merge(T entity) {
        this.checkOpen();
        EntityMetadata metadata = metadataManager.getEntityMetadata(entity.getClass());
        Object id = metadata.getId(entity);

        if (id == null) throw new IllegalStateException("Cannot merge entity without id");

        Map<String, Object> values = metadata.extractValues(entity);
        String tableName = metadata.getTableName();
        String idColumn = metadata.getIdColumn();

        //Construir query de actualizacion
        StringBuilder sql = new StringBuilder(String.format("UPDATE %s SET ", tableName));
        String setClause = values.keySet().stream()
                .filter(o -> !o.equals(idColumn))
                .map(o -> o + " = ?")
                .collect(Collectors.joining(", "));

        sql.append(setClause).append(" WHERE ").append(idColumn).append(" = ?");

        List<Object> params = new ArrayList<>(values.values());
        params.add(id);
        //ACA QUERYEXECUTOR

        cacheManager.putEntity(entity.getClass(), id, entity);
        cacheManager.invalidateQueriesForTable(tableName);

        return entity;
    }

    @Override
    public void remove(Object entity) {

    }

    @Override
    public void flush() {

    }

    @Override
    public void clear() {

    }

    @Override
    public void detach(Object entity) {

    }

    @Override
    public void close() throws Exception {

    }

    private void checkOpen() {
        if (!isOpen) throw new IllegalStateException("EntityManager is closed");
    }
}
