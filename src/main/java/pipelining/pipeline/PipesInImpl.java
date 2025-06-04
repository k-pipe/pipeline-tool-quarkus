package pipelining.pipeline;

import pipelining.application.Application;
import pipelining.job.implementation.Workdir;
import pipelining.json.Json;
import pipelining.util.Utf8;
import pipelining.logging.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static pipelining.logging.Log.onException;

public class PipesInImpl<D> implements PipesIn<D> {

	private final Map<String, Iterable<D>> pipes;
	private final Class<D> itemClass;

	public PipesInImpl(final String suffix, final Workdir workdir, Class<D> itemClass) {
		this.pipes = createPipes(suffix, workdir);
		this.itemClass = itemClass;
	}

	@Override
	public Map<String, Iterable<D>> getInputPipes() {
		return pipes;
	}

	private Map<String, Iterable<D>> createPipes(final String suffix, final Workdir workdir) {
		Map<String, Iterable<D>> result = new LinkedHashMap<>();
		String tail = (suffix == null) || suffix.isBlank() ? null : "."+suffix;
		workdir.listInputs(tail).forEach(in -> result.put(in, createIterable(workdir.getInput(in))));
		return result;
	}

	private Iterable<D> createIterable(final Path path) {
		return () -> Log.onException(() -> createIterator(path)).fail("Could not iterate over input pipe file "+path);
	}

	private Iterator<D> createIterator(final Path path) throws IOException {
		InputStream in = new FileInputStream(path.toFile());
		Iterator<String> lineIterator = Utf8.lineIterator(Application.isInProduction() ? new GZIPInputStream(in) : in);
		return new Iterator<>() {
			@Override
			public boolean hasNext() {
				return lineIterator.hasNext();
			}

			@Override
			public D next() {
				return Json.fromString(lineIterator.next(), itemClass);
			}
		};
	}

}
