package pipelining.job.pipeline;

import pipelining.job.All;
import pipelining.job.annotations.Input;
import pipelining.job.annotations.Output;
import pipelining.pipeline.PipesIn;
import pipelining.pipeline.PipesOut;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import static pipelining.logging.Log.onException;

/**
 *
 * @param <C> the class of the configuration data
 * @param <DI> the class of the items in the input pipeline
 * @param <DO> the class of the items in the output pipeline
 */
public abstract class TransformingPipelineJob<C, DI, DO> extends BasePipelineJob {

	// capturing the generic arguments
	protected Type typeC;
	protected Type typeDI;
	protected Type typeDO;

	private boolean pushedSomething;

	@Input("config.json")
	protected C config;

	@Input("pipe")
	private PipesIn<DI> inputs;

	@Output("pipe")
	private PipesOut<DO, All> outputs;

	protected PipesIn<?> inputPipes() {
		return inputs;
	}

	protected Class<?> outEnum() {
		return All.class;
	}

	@Override
	public void captureTypes() {
		ParameterizedType pt = getParameterizedType(TransformingPipelineJob.class);
		final Type[] args = pt.getActualTypeArguments();
		typeC = args[0];
		typeDI = args[1];
		typeDO = args[2];
	}

	@Override
	public Class<?> getGenericTypeC() { return (Class<?>)typeC; }

	@Override
	public Class<?> getGenericTypeD() {
		return (Class<?>)typeDI;
	}

	@Override
	public Class<?> getGenericTypeE() {
		return All.class;
	}

	/**
	 * Transform an item
	 *
	 * @param inputItem the item to be transformed
	 * @param outputConsumer consumes the result(s) of the transformation, can be called multiple times for a input
	 *                        item or not at all for input items considered discarded
	 */
	protected abstract void transform(DI inputItem, Consumer<DO> outputConsumer);

	@Override
	public void run() {
		init();
		beforeProcessing();
		inputs.getInputPipes().forEach((name, pipe) -> pipe.forEach(item -> transformAndPush(name, item)));
		afterProcessing();
		outputs.closeAll();
		logSummary();
	}

	protected void afterProcessing() {
	}

	private void transformAndPush(final String pipeName, final DI item) {
		onException(() -> {
			numItemsProcessed++;
			incNumIn(pipeName);
			pushedSomething = false;
			transform(item, this::push);
			if (!pushedSomething) {
				numItemsDiscarded++;
			}
		}).handle(e -> {
			numItemsFailed++;
		});
	}

	protected void push(final DO outputItem) {
		outputs.pushTo(outputItem, All.ALL);
		String key = All.ALL.toString();
		incNumOut(key);
		pushedSomething = true;
	}

}
