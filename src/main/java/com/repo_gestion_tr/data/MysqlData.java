// src/main/java/com/repo_gestion_tr/data/MysqlData.java
package com.repo_gestion_tr.data;

import com.repo_gestion_tr.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class MysqlData {
    
    private static Connection connection;

    private MysqlData() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                AppConfig.mysqlUrl(), AppConfig.mysqlUser(), AppConfig.mysqlPassword());
        }
        return connection;
    }
}

