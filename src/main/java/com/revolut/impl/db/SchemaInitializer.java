package com.revolut.impl.db;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Have to setup schema every time as we're running an in-memory database.
 * 
 * @author verevic
 *
 */
class SchemaInitializer {
	private static final String PATH = "schema";
	private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

	private static void scanPath(Path path, ScriptRunner runner) throws IOException, SQLException {
		if (Files.isRegularFile(path)) {
			if (path.toString().toLowerCase().endsWith(".sql")) {
				log.info("Reading schema from the {} file", path.toString());
				runner.runScript(Files.newBufferedReader(path));
			}
		} else {
			log.info("Scanning {} folder for script files", path.toString());
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
				for (Path child : ds) {
					scanPath(child, runner);
				}
			}
		}
	}

	static void setup(Connection connection) throws IOException, SQLException {
		URL root = SchemaInitializer.class.getClassLoader().getResource(PATH);
		if (root == null) {
			log.error("Cannot find {} folder", PATH);
			return;
		}

		Path path = Paths.get(URI.create(root.toString()));
		ScriptRunner runner = new ScriptRunner(connection, true, true);
		scanPath(path, runner);
	}
}
