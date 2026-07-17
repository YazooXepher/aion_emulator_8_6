package com.aionemu.commons.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * <b>Database Factory</b><br>
 * <br>
 * This file is used for creating a pool of connections for the server.<br>
 * It utilizes database.properties and creates a pool of connections and
 * automatically recycles them when closed.<br>
 * <br>
 * DB.java utilizes the class.<br>
 */
public class DatabaseFactory {

	private static final Logger log = LoggerFactory.getLogger(DatabaseFactory.class);

	private static HikariDataSource connectionPool;

	private static String databaseName;
	private static int databaseMajorVersion;
	private static int databaseMinorVersion;

	public synchronized static void init() {
		if (connectionPool != null) {
			return;
		}

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(DatabaseConfig.DATABASE_URL);
		config.setUsername(DatabaseConfig.DATABASE_USER);
		config.setPassword(DatabaseConfig.DATABASE_PASSWORD);
		config.setMaximumPoolSize(DatabaseConfig.DATABASE_BONECP_PARTITION_CONNECTIONS_MAX);
		config.setMinimumIdle(DatabaseConfig.DATABASE_BONECP_PARTITION_CONNECTIONS_MIN);

		try {
			Class.forName(DatabaseConfig.DATABASE_DRIVER.getName());
		} catch (ClassNotFoundException e) {
			log.error("Error obtaining DB driver", e);
			throw new Error("DB Driver doesnt exist!");
		}

		try {
			connectionPool = new HikariDataSource(config);
		} catch (Exception e) {
			log.error("Error while creating DB Connection pool", e);
			throw new Error("DatabaseFactory not initialized!", e);
		}

		try {
			Connection c = getConnection();
			DatabaseMetaData dmd = c.getMetaData();
			databaseName = dmd.getDatabaseProductName();
			databaseMajorVersion = dmd.getDatabaseMajorVersion();
			databaseMinorVersion = dmd.getDatabaseMinorVersion();
			c.close();
		} catch (Exception e) {
			log.error("Error with connection string: " + DatabaseConfig.DATABASE_URL, e);
			throw new Error("DatabaseFactory not initialized!");
		}

		log.info("Successfully connected to database");
	}

	public static Connection getConnection() throws SQLException {
		Connection con = connectionPool.getConnection();

		if (!con.getAutoCommit()) {
			log.error("Connection Settings Error: Connection obtained from database factory should be in auto-commit"
					+ " mode. Forsing auto-commit to true. Please check source code for connections beeing not properly"
					+ " closed.");
			con.setAutoCommit(true);
		}

		return con;
	}

	public int getActiveConnections() {
		return connectionPool.getHikariPoolMXBean().getActiveConnections();
	}

	public int getIdleConnections() {
		return connectionPool.getHikariPoolMXBean().getIdleConnections();
	}

	public static synchronized void shutdown() {
		try {
			if (connectionPool != null) {
				connectionPool.close();
			}
		} catch (Exception e) {
			log.warn("Failed to shutdown DatabaseFactory", e);
		}
		connectionPool = null;
	}

	public static void close(PreparedStatement st, Connection con) {
		close(st);
		close(con);
	}

	public static void close(PreparedStatement st) {
		if (st == null) {
			return;
		}
		try {
			if (!st.isClosed()) {
				st.close();
			}
		} catch (SQLException e) {
			log.error("Can't close Prepared Statement", e);
		}
	}

	public static void close(Connection con) {
		if (con == null)
			return;
		try {
			if (!con.getAutoCommit()) {
				con.setAutoCommit(true);
			}
		} catch (SQLException e) {
			log.error("Failed to set autocommit to true while closing connection: ", e);
		}
		try {
			con.close();
		} catch (SQLException e) {
			log.error("DatabaseFactory: Failed to close database connection!", e);
		}
	}

	public static String getDatabaseName() {
		return databaseName;
	}

	public static int getDatabaseMajorVersion() {
		return databaseMajorVersion;
	}

	public static int getDatabaseMinorVersion() {
		return databaseMinorVersion;
	}

	private DatabaseFactory() {
	}
}