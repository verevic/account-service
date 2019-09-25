package com.revolut.dao;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolut.dao.JDBCUtils.ThrowableConsumer;

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
				for (Path child : StreamSupport.stream(ds.spliterator(), false).sorted((path1, path2) -> {
						return path1.toString().compareTo(path2.toString());
					}).collect(Collectors.toList())) {
					scanPath(child, runner);
				}
			}
		}
	}

	static void setup(Connection connection) throws SQLException {
		log.info("Initializing DB schema");
		URL root = SchemaInitializer.class.getClassLoader().getResource(PATH);
		if (root == null) {
			log.error("Cannot find {} folder", PATH);
			return;
		}

		log.info(String.format("URL: %s", root.toString()));
		URI uri = URI.create(root.toString());
		log.info(String.format("URI: %s, scheme: %s", uri.toString(), uri.getScheme()));
		// https://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html
		ThrowableConsumer<Callable<Void>> fsFix = "jar".contentEquals(uri.getScheme()) ?
				c -> {
			        Map<String, String> env = Collections.singletonMap("create", "true");
			        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
			        	c.call();
			        }					
				} : Callable::call;
		try {
			fsFix.accept(() -> {
				Path path = Paths.get(uri);
				log.info(String.format("Path: %s", path.toString()));
				ScriptRunner runner = new ScriptRunner(connection, true, true);
				log.info(String.format("Start scanning %s", path.toString()));
				scanPath(path, runner);
				return null;
			});
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new SQLException("Failed to initialize DB schema");
		}
		log.info("DB schema initialization succeeded");
	}
}
