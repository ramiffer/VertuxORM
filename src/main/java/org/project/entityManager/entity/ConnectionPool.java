package org.project.entityManager.entity;


import javax.sql.DataSource;

public class ConnectionPool {

    private final DataSource dataSource;

    public ConnectionPool(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
