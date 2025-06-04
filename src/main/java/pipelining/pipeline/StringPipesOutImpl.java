package pipelining.pipeline;

import pipelining.job.implementation.Workdir;
import pipelining.json.Json;

import java.io.PrintStream;

public class StringPipesOutImpl<D> extends AbstractPipesOutImpl<D, String> {

	public StringPipesOutImpl(String name, final Workdir workdir) {
		super(name, workdir);
	}

	@Override
	public void pushTo(final D item, final String pipe) {
		PrintStream out = pipe == null ? failedPipe : pipes.get(pipe);
	    if (out == null) {
			out = createOutPipe(pipe);
			pipes.put(pipe, out);
		}
		out.println(Json.toString(item));
	}

}
