package com.repo_gestion_tr.jpa;

import com.repo_gestion_tr.config.AppConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public final class JpaUtil {

    private static EntityManagerFactory EMF;

    private JpaUtil() {}

    public static EntityManagerFactory emf() {
        if (EMF == null) {
            Map<String, Object> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.url", AppConfig.pgUrl());
            props.put("jakarta.persistence.jdbc.user", AppConfig.pgUser());
            props.put("jakarta.persistence.jdbc.password", AppConfig.pgPassword());
            props.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");

            props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.put("hibernate.hbm2ddl.auto", "validate");
            props.put("hibernate.show_sql", "false");
            props.put("hibernate.format_sql", "true");

            EMF = Persistence.createEntityManagerFactory("gestionTR-PU", props);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { if (EMF != null && EMF.isOpen()) EMF.close(); } catch (Exception ignored) {}
            }));
        }
        return EMF;
    }

    public static EntityManager createEntityManager() {
        return emf().createEntityManager();
    }
}
