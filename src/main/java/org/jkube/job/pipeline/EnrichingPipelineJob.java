package org.jkube.job.pipeline;

import org.jkube.job.All;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @param <C> the class of the configuration data
 * @param <D> the class of the items flowing in the pipeline ("documents")
 */
public abstract class EnrichingPipelineJob<C, D> extends GenericPipelineJob<C, D, All> {

	@Override
	public void captureTypes() {
		ParameterizedType pt = getParameterizedType(EnrichingPipelineJob.class);
		final Type[] args = pt.getActualTypeArguments();
		typeC = args[0];
		typeD = args[1];
		typeE = All.class;
	}

	protected abstract void enrich(final D item);

	@Override
	protected All enrichAndAssign(final D item) {
		enrich(item);
		return All.ALL;
	}

}
