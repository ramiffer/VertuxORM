package org.project.entityManager.configuration;

import javax.sql.DataSource;
import java.time.Duration;

public interface EntityManagerConfig {

    DataSource getDataSource();
    Duration getDefaultCacheTtl();
}
