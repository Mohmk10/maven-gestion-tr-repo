package com.repo_gestion_tr.data;

import com.repo_gestion_tr.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class MysqlData {
    private MysqlData() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            AppConfig.mysqlUrl(),
            AppConfig.mysqlUser(),
            AppConfig.mysqlPassword()
        );
    }
}
