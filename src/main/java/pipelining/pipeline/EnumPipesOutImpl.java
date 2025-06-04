package pipelining.pipeline;

import pipelining.job.implementation.Workdir;
import pipelining.json.Json;

import java.util.List;

public class EnumPipesOutImpl<D, E extends Enum<E>> extends AbstractPipesOutImpl<D, E> {

	public EnumPipesOutImpl(String name, final Workdir workdir, List<E> values) {
		super(name, workdir);
		for (E value : values) {
			registerOutputValue(value);
		}
	}

	@Override
	public void pushTo(final D item, final E pipe) {
		(pipe == null ? failedPipe : pipes.get(pipe)).println(Json.toString(item));
	}

}
