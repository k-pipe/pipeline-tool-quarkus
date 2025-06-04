package pipelining.pipeline;

import pipelining.application.Application;
import pipelining.job.implementation.Workdir;
import pipelining.util.Utf8;
import pipelining.logging.Log;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static pipelining.logging.Log.onException;

public abstract class AbstractPipesOutImpl<D, E> implements PipesOut<D, E> {

	public static final String FAILED_PIPE_NAME = "FAILED";
	protected final Map<E, PrintStream> pipes = new LinkedHashMap<>();
	private final Workdir workdir;
	private final String suffix;
	protected final PrintStream failedPipe;

	protected AbstractPipesOutImpl(String name, final Workdir workdir) {
		this.workdir = workdir;
		this.suffix = (name == null) || name.isEmpty() ? "" : "."+name;
		this.failedPipe = createOutPipe(FAILED_PIPE_NAME);
	}

	public void registerOutputValue(final E pipeValue) {
		pipes.put(pipeValue, createOutPipe(pipeValue.toString().toLowerCase()));
	}

	protected PrintStream createOutPipe(final String name) {
		return Log.onException(() -> {
			OutputStream out = new FileOutputStream(workdir.getOutput(name+suffix).toFile());
			if (Application.isInProduction()) {
				out = new GZIPOutputStream(out);
			}
			return Utf8.printStream(out);
		}).fail("Could not create output pipeline file " + name+suffix);
	}

	@Override
	public abstract void pushTo(D item, E result);

	@Override
	public void closeAll() {
		pipes.values().forEach(PrintStream::close);
		failedPipe.close();
	}
}
