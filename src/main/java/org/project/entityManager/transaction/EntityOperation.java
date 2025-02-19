package org.project.entityManager.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class EntityOperation {

    private final String sql;
    private final List<Object> parameters;
    private final OperationType type;

    public enum OperationType {
        INSERT, UPDATE, DELETE
    }

    public EntityOperation(String sql, List<Object> parameters, OperationType type) {
        this.sql = sql;
        this.parameters = parameters;
        this.type = type;
    }

    public void execute(Connection connection) throws SQLException {
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            preparedStatement.executeUpdate();
        }
    }
}
