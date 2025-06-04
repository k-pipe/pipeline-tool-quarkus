package pipelining.job.pipeline;

import pipelining.job.All;
import pipelining.logging.Log;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static pipelining.logging.Log.onException;

/**
 *
 * @param <C> the class of the configuration data
 * @param <D> the class of the items flowing in the pipeline ("documents")
 */
public abstract class MicroBatchedPipelineJob<C, D> extends GenericPipelineJob<C, D, All> {

	private final List<D> batch = new ArrayList<>();
	private final int maxBatchSize = getMicroBatchSize();

	@Override
	public void captureTypes() {
		ParameterizedType pt = getParameterizedType(MicroBatchedPipelineJob.class);
		final Type[] args = pt.getActualTypeArguments();
		typeC = args[0];
		typeD = args[1];
		typeE = All.class;
	}

	/**
	 * Provides the maximal size of the micro batches (if less or equal 0: process all documents in one batch)
	 *
	 * @return maximal size of micro batches
	 */
	protected abstract int getMicroBatchSize();

	/**
	 * Process a micro batch of items
	 *
	 * @param microBatch the documents to be processed
	 * @return true if successful, if retuned false: all documents in the microBatch are considered failed
	 */
	protected abstract boolean enrich(final List<D> microBatch);

	@Override
	public void run() {
		init();
		beforeProcessing();
		inputs.getInputPipes().forEach((name, pipe) -> pipe.forEach(item -> addToBatch(name, item)));
		if (!batch.isEmpty()) {
			processBatch();
		}
		afterProcessing();
		outputs.closeAll();
		logSummary();
	}

	private void addToBatch(final String pipeName, final D item) {
		incNumIn(pipeName);
		batch.add(item);
		if ((maxBatchSize > 0) && (batch.size() >= maxBatchSize)) {
			processBatch();
		}
	}

	private void processBatch() {
		boolean success;
		Log.onException(() -> {
			numItemsProcessed += batch.size();
			if (enrich(batch)) {
				push(All.ALL, batch);
				incNumOut(All.ALL.toString(), batch.size());
			} else {
				numItemsDiscarded += batch.size();
			}
		}).handle(e -> {
			push(null, batch);
			numItemsFailed += batch.size();
		});
		batch.clear();
	}

	protected void push(final All outpipe, final List<D> batch) {
		batch.forEach(item -> outputs.pushTo(item, outpipe));
	}

}
