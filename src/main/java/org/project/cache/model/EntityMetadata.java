package org.project.cache.model;

import java.util.HashMap;
import java.util.Map;

public class EntityMetadata {

    private String tableName;
    private String getIdColumn;
    private Object id;


    public <T> Map<String, Object> extractValues(T entity) {
        return new HashMap<String, Object>();
    }

    public String getTableName() {
        return tableName;
    }

    public String getIdColumn() {
        return getIdColumn;
    }

    public <T> void setId(T entity, Object id) {

    }

    public <T> Object getId(T entity) {
        return id;
    }
}
