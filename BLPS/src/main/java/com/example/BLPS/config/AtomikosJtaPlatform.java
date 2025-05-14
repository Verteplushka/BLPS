package com.example.BLPS.config;

import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;

import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

public class AtomikosJtaPlatform extends AbstractJtaPlatform {

    private static final long serialVersionUID = 1L;

    static TransactionManager transactionManager;
    static UserTransaction transaction;

    @Override
    protected TransactionManager locateTransactionManager() {
        return transactionManager;
    }

    @Override
    protected UserTransaction locateUserTransaction() {
        return transaction;
    }
}
