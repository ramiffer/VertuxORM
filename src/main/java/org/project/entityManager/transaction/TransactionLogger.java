package org.project.entityManager.transaction;

import java.util.logging.Logger;

public class TransactionLogger {

    private static final Logger log = Logger.getLogger(String.valueOf(TransactionLogger.class));

    //PENSAR ESTO
    public void logTransactionStart() {
        log.info("Transaction started");
    }

    public void logTransactionRolledBack() {
        log.info("Transaction rolled back");
    }

    public void logTransactionCommit() {
        log.info("Transaction commited successfully");
    }

    public void logTransactionError(Exception e) {
        log.info("Transaction error: " + e.getMessage());
    }

    public void logRollbackError(Exception e) {
        log.info("Rollback error: " + e.getMessage());
    }

}
