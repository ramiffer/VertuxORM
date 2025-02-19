package org.project.entityManager.configuration.impl;

import org.project.entityManager.configuration.EntityManagerConfig;

import javax.sql.DataSource;
import java.time.Duration;

public class EntityManagerConfigImpl implements EntityManagerConfig {

    @Override
    public DataSource getDataSource() {
        return null;
    }

    @Override
    public Duration getDefaultCacheTtl() {
        return null;
    }
}
