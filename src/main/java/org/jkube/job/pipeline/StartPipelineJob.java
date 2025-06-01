package org.jkube.job.pipeline;

import org.jkube.job.All;
import org.jkube.job.annotations.Input;
import org.jkube.job.annotations.Output;
import org.jkube.pipeline.PipesIn;
import org.jkube.pipeline.PipesOut;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

import static org.jkube.logging.Log.onException;

/**
 *
 * @param <C> the class of the configuration data
 * @param <D> the class of the items in the output pipeline
 */
public abstract class StartPipelineJob<C, D> extends BasePipelineJob {

	// capturing the generic arguments
	protected Type typeC;
	protected Type typeD;

	@Input("config.json")
	protected C config;

	@Input("runIdentifier")
	protected Optional<File> previousRunIdentifier;

	@Output("pipe")
	private PipesOut<D, All> outputs;

	@Output("runIdentifier")
	protected String thisRunIdentifier;

	protected Class<?> outEnum() {
		return All.class;
	}

	@Override
	public void captureTypes() {
		ParameterizedType pt = getParameterizedType(StartPipelineJob.class);
		final Type[] args = pt.getActualTypeArguments();
		typeC = args[0];
		typeD = args[1];
	}

	@Override
	public Class<?> getGenericTypeC() { return (Class<?>)typeC; }

	@Override
	public Class<?> getGenericTypeD() {
		return (Class<?>)typeD;
	}

	@Override
	public Class<?> getGenericTypeE() {
		return All.class;
	}

	/**
	 * Create items as input for the pipeline.
	 *
	 * @param  previousRunIdentifier a reference of the last successful run of the pipeline (e.g. timestamp), null if this is the first execution of the pipeline
	 * @param outputConsumer consumes the generated start items
	 * @return a reference of the current run (will be possed to next pipeline run, if this pipeline executed successfully)
	 */
	protected abstract String createItems(String previousRunIdentifier, Consumer<D> outputConsumer);

	@Override
	public void run() {
		init();
		beforeProcessing();
		thisRunIdentifier = createItems(readOptionalFile(previousRunIdentifier), this::pushItem);
		afterProcessing();
		outputs.closeAll();
		logSummary();
	}

	private String readOptionalFile(final Optional<File> file) {
		return file.isEmpty() ? null : onException(() -> Files.readString(file.get().toPath())).fail("Could not load file "+file.get());
	}

	protected void afterProcessing() {
	}

	protected void pushItem(final D outputItem) {
		outputs.pushTo(outputItem, All.ALL);
		String key = All.ALL.toString();
		incNumOut(key);
	}

	@Override
	protected PipesIn<?> inputPipes() {
		return (PipesIn<Object>) Collections::emptyMap;
	}

}
