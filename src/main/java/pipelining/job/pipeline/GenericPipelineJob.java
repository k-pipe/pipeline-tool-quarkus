package pipelining.job.pipeline;

import pipelining.job.annotations.Input;
import pipelining.job.annotations.Output;
import pipelining.pipeline.PipesIn;
import pipelining.pipeline.PipesOut;
import pipelining.logging.Log;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static pipelining.logging.Log.onException;

/**
 *
 * @param <C> the class of the configuration data
 * @param <D> the class of the items flowing in the pipeline ("documents")
 * @param <E> the enum branching class that is used to determine into which output pipe an item is pushed
 */
public abstract class GenericPipelineJob<C, D, E> extends BasePipelineJob {

	// capturing the generic arguments
	protected Type typeC;
	protected Type typeD;
	protected Type typeE;

	@Input("config.json")
	protected C config;

	@Input("pipe")
	protected PipesIn<D> inputs;

	@Output("pipe")
	protected PipesOut<D, E> outputs;

	protected PipesIn<D> inputPipes() {
		return inputs;
	}

	protected PipesOut<D, E> outputPipes() {
		return outputs;
	}

	protected Class<E> outEnum() {
		return getGenericTypeE();
	}

	@Override
	public void captureTypes() {
		ParameterizedType pt = getParameterizedType(GenericPipelineJob.class);
		final Type[] args = pt.getActualTypeArguments();
		typeC = args[0];
		typeD = args[1];
		typeE = args[2];
	}

	/**
	 * Process an item, determine which output pipe the item shall be put into
	 *
	 * @param item the item to be processed, changes to the passed object will be reflected in the output
	 * @return enum value that represents the desired output pipe, can be null, in which case the item will be discarded
	 */
	protected abstract E enrichAndAssign(D item);

	@Override
	public void run() {
		init();
		beforeProcessing();
		inputs.getInputPipes().forEach((name, pipe) -> pipe.forEach(item -> processAndPush(name, item)));
		afterProcessing();
		outputs.closeAll();
		logSummary();
	}

	@Override
	public Class<C> getGenericTypeC() { return (Class<C>)typeC; }

	@Override
	public Class<D> getGenericTypeD() {
		return (Class<D>)typeD;
	}

	@Override
	public Class<E> getGenericTypeE() {
		return (Class<E>)typeE;
	}

	private void processAndPush(final String pipeName, final D item) {
		Log.onException(() -> {
			numItemsProcessed++;
			incNumIn(pipeName);
			E outpipe = enrichAndAssign(item);
			outputs.pushTo(item, outpipe);
			if (outpipe == null) {
				numItemsDiscarded++;
			} else {
				String key = outpipe.toString();
				incNumOut(key);
			}
		}).handle(e -> {
			outputs.pushTo(item, null);
			numItemsFailed++;
		});
	}

}
