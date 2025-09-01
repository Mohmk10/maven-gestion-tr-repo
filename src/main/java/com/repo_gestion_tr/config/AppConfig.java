package com.repo_gestion_tr.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;


public final class AppConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (is == null) {
                throw new IllegalStateException("app.properties introuvable dans le classpath (src/main/resources).");
            }
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                PROPS.load(reader);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors du chargement de app.properties : " + e.getMessage(), e);
        }
    }

    private AppConfig() {
        
    }


    private static String raw(String key) {
        String sys = System.getProperty(key);
        if (sys != null) return sys;
        return PROPS.getProperty(key);
    }


    private static String trimOrNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }


    private static String required(String key) {
        String v = trimOrNull(raw(key));
        if (v == null) {
            throw new IllegalStateException("Clé manquante ou vide dans app.properties : " + key);
        }
        return v;
    }

    
    public static String dbType() {
        String v = required("db.type").toLowerCase(Locale.ROOT);
        if (!Objects.equals(v, "mysql") && !Objects.equals(v, "postgres")) {
            throw new IllegalStateException("Valeur invalide pour db.type : " + v + " (attendu: mysql | postgres)");
        }
        return v;
    }

    /* ===================== Postgres (Neon) ===================== */

    public static String pgUrl() {
        return required("db.postgres.url");
    }

    public static String pgUser() {
        return required("db.postgres.username");
    }

    public static String pgPassword() {
        return required("db.postgres.password");
    }

    /* ===================== MySQL (local) ===================== */

    public static String mysqlUrl() {
        return required("db.mysql.url");
    }

    public static String mysqlUser() {
        return required("db.mysql.username");
    }

    public static String mysqlPassword() {
        return required("db.mysql.password");
    }

}

