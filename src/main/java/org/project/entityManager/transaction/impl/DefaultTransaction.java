package org.project.entityManager.transaction.impl;

import org.project.entityManager.transaction.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultTransaction implements Transaction {

    private final Connection connection;
    private TransactionStatus status;
    private final Set<EntityOperation> pendingOperations;
    private final TransactionLogger logger;

    public DefaultTransaction(Connection connection) {
        this.connection = connection;
        this.status = TransactionStatus.NOT_STARTED;
        this.pendingOperations = new LinkedHashSet<>();
        this.logger = new TransactionLogger();
    }


    @Override
    public void begin() {
        if (status == TransactionStatus.ACTIVE) {
            throw new IllegalStateException("Transaction is already active");
        }

        try {
            connection.setAutoCommit(false);
            status = TransactionStatus.ACTIVE;
            logger.logTransactionStart();
        } catch (SQLException e) {
            status = TransactionStatus.FAILED;
            throw new TransactionException("Failed to start transaction", e);
        }
    }

    @Override
    public void commit() {
        this.checkActive();

        try {

            for (EntityOperation operation : pendingOperations) {
                operation.execute(connection);
            }

            connection.commit();
            status = TransactionStatus.COMMITTED;
            logger.logTransactionCommit();

            connection.setAutoCommit(true);

            pendingOperations.clear();
        } catch (SQLException e) {
            this.handleCommitFailure(e);
        }
    }

    @Override
    public void rollback() {
        if (status != TransactionStatus.ACTIVE && status != TransactionStatus.FAILED) {
            throw new IllegalStateException("Transaction is not active or faile");
        }

        try {
            connection.rollback();
            status = TransactionStatus.ROLLED_BACK;
            logger.logTransactionRolledBack();
            connection.setAutoCommit(true);
            pendingOperations.clear();
        } catch (SQLException e) {
            throw new TransactionException("Failed to rollback transaction", e);
        }
    }

    @Override
    public boolean isActive() {
        return status == TransactionStatus.ACTIVE;
    }

    @Override
    public TransactionStatus getStatus() {
        return status;
    }

    public void addOperation(EntityOperation operation) {
        this.checkActive();
        pendingOperations.add(operation);
    }

    private void checkActive() {
        if (status != TransactionStatus.ACTIVE) {
            throw new IllegalStateException("Transaction is not active");
        }
    }

    private void handleCommitFailure(SQLException e) {
        status = TransactionStatus.FAILED;
        logger.logTransactionError(e);
        try {
            connection.rollback();
            logger.logTransactionRolledBack();
        } catch (SQLException rollbackEx) {
            logger.logRollbackError(rollbackEx);
        }
        throw new TransactionException("Failed to commit transaction", e);
    }

}
