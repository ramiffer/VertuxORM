package org.project.entityManager.transaction;

public interface Transaction {

    void begin();
    void commit();
    void rollback();
    boolean isActive();
    TransactionStatus getStatus();
}
