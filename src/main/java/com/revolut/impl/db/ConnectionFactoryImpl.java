package com.revolut.impl.db;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolut.db.ConnectionFactory;

class ConnectionFactoryImpl implements ConnectionFactory {
	private static final String PROPERTIES_FILE_NAME = "jdbc-config.xml";

	private static final Logger log = LoggerFactory.getLogger(ConnectionFactory.class);
	private static final Properties properties;

	static {
		properties = new Properties();
		ClassLoader cl = ConnectionFactory.class.getClassLoader();
		URL url = cl.getResource(PROPERTIES_FILE_NAME);
		if (url == null) {
			log.error(String.format("Cannot find file \"%s\"", PROPERTIES_FILE_NAME));
		} else {
			log.info("Reading connection properties from {}", url.toString());
			try (InputStream is = url.openStream()) {
				properties.loadFromXML(is);
			} catch (IOException e) {
				log.error("Error reading connection properties from {}", url.toString());
			}
		}
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(
				properties.getProperty("jdbc.url"),
				properties.getProperty("jdbc.username"),
				properties.getProperty("jdbc.password"));
	}
}
