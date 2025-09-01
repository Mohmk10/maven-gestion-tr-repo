package com.repo_gestion_tr.container;

import com.repo_gestion_tr.config.AppConfig;

import com.repo_gestion_tr.repository.CompteRepository;
import com.repo_gestion_tr.repository.TransactionRepository;

import com.repo_gestion_tr.repository.jdbc.CompteRepositoryJdbc;
import com.repo_gestion_tr.repository.jdbc.TransactionRepositoryJdbc;

import com.repo_gestion_tr.repository.jpa.CompteRepositoryJpa;
import com.repo_gestion_tr.repository.jpa.TransactionRepositoryJpa;

public final class Factory {

    private Factory() {}

    public enum Impl { JDBC, JPA }

    private static Impl impl = null;
    private static CompteRepository COMPTE_REPO;
    private static TransactionRepository TRANSACTION_REPO;

    
    public static void configure(Impl chosen) {
        if (impl != null)
            throw new IllegalStateException("Factory déjà initialisée en " + impl);
        impl = chosen;
    }

    
    private static Impl resolvedImpl() {
        if (impl != null) return impl;
        String t = AppConfig.dbType();
        impl = "postgres".equalsIgnoreCase(t) ? Impl.JPA : Impl.JDBC;
        return impl;
    }

    public static CompteRepository compteRepository() {
        if (COMPTE_REPO == null) {
            switch (resolvedImpl()) {
                case JPA  -> COMPTE_REPO = new CompteRepositoryJpa();
                case JDBC -> COMPTE_REPO = new CompteRepositoryJdbc();
            }
        }
        return COMPTE_REPO;
    }

    public static TransactionRepository transactionRepository() {
        if (TRANSACTION_REPO == null) {
            switch (resolvedImpl()) {
                case JPA  -> TRANSACTION_REPO = new TransactionRepositoryJpa();
                case JDBC -> TRANSACTION_REPO = new TransactionRepositoryJdbc();
            }
        }
        return TRANSACTION_REPO;
    }
}
