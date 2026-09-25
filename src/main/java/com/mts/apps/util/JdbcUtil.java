package com.mts.apps.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcUtil {

    private static final Logger logger = LoggerFactory.getLogger(JdbcUtil.class);

    private static final Properties props = new Properties();

    // runs once, the first time JdbcUtil is used
    static {
        try (InputStream in = JdbcUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                logger.error("db.properties not found on the classpath");
                throw new RuntimeException("db.properties not found in src/main/resources");
            }
            props.load(in);
            logger.info("Loaded db.properties, url={}, user={}",
                    props.getProperty("db.url"), props.getProperty("db.user"));
        } catch (IOException e) {
            logger.error("Could not read db.properties", e);
            throw new RuntimeException("Could not read db.properties", e);
        }
    }

    public Connection getConnectionObject() throws SQLException {
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        try {
            Connection con = DriverManager.getConnection(url, user, props.getProperty("db.password"));
            logger.debug("Connection opened to {}", url);
            return con;
        } catch (SQLException e) {
            logger.error("Could not connect to {} as user {}", url, user, e);
            throw e;
        }
    }
}