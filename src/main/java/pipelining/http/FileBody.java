package pipelining.http;

import pipelining.logging.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Supplier;

import static pipelining.logging.Log.onException;

public class FileBody implements Supplier<InputStream> {

	private final File file;

	public FileBody(final Path path) {
		this.file = path.toFile();
	}

	@Override
	public InputStream get() {
		return Log.onException(() -> new FileInputStream(file)).fail("Could not find file "+file);
	}
}
