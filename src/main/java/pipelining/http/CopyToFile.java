package pipelining.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static pipelining.logging.Log.debug;

public class CopyToFile implements StreamReader<String> {

	private final Path path;

	public CopyToFile(final Path path) {
		this.path = path;
	}

	@Override
	public String read(final InputStream body) throws IOException {
		Files.copy(body, path, StandardCopyOption.REPLACE_EXISTING);
		String message = "downloaded "+path.toFile().length()+" bytes to "+path;
		debug(message);
		return message;
	}

}
