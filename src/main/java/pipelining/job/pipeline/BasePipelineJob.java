package pipelining.job.pipeline;

import pipelining.application.Application;
import pipelining.job.Job;
import pipelining.pipeline.PipelineProcessingException;
import pipelining.pipeline.PipesIn;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.Map;

import static pipelining.application.Application.fail;
import static pipelining.logging.Log.log;

public abstract class BasePipelineJob implements Job {

	private Map<String, Integer> numItemsIn;
	private Map<String, Integer> numItemsOut;
	protected int numItemsFailed;
	protected int numItemsProcessed;
	protected int numItemsDiscarded;

	public void incNumIn(String pipe, int inc) {
		numItemsIn.put(pipe, numItemsIn.getOrDefault(pipe, 0)+inc);
	}

	public void incNumIn(String pipe) {
		incNumIn(pipe, 1);
	}

	public void incNumOut(String pipe, int inc) {
		numItemsOut.put(pipe, numItemsOut.getOrDefault(pipe, 0)+inc);
	}

	public void incNumOut(String pipe) {
		incNumOut(pipe, 1);
	}

	private static void installFailureHandler() {
		Application.setFailureHandler((message, failureCode) -> {
			throw new PipelineProcessingException(message);
		});
	}

	protected abstract PipesIn<?> inputPipes();

	protected abstract Class<?> outEnum();

	public abstract void captureTypes();

	public abstract Class<?> getGenericTypeC();

	public abstract Class<?> getGenericTypeD();

	public abstract Class<?> getGenericTypeE();

	protected void init() {
		numItemsIn = new LinkedHashMap<>();
		inputPipes().getInputPipes().keySet().forEach(name -> numItemsIn.put(name, 0));
		numItemsOut = new LinkedHashMap<>();
		numItemsProcessed = 0;
		numItemsFailed = 0;
		numItemsDiscarded = 0;
	}

	protected void beforeProcessing() {
	}

	protected void afterProcessing() {
	}

	private String statistics(final String type, final Map<?, Integer> numItems) {
		StringBuilder sb = new StringBuilder();
		sb.append("Statistics of ");
		sb.append(type);
		sb.append(" pipelines:");
		numItems.forEach((name, num) -> {
			sb.append("\n");
			sb.append(name);
			sb.append(":\t");
			sb.append(num);
		});
		return sb.toString();
	}

	protected void logSummary() {
		log("Processing of {} documents done, {} of them were discarded, {} of them failed", numItemsProcessed,
				numItemsDiscarded, numItemsFailed);
		log(statistics("input", numItemsIn));
		log(statistics("output", numItemsOut));
	}

	protected ParameterizedType getParameterizedType(Class<?> genericAncestor) {
		Class<?> clazz = getClass();
		while (!clazz.getSuperclass().equals(genericAncestor)) {
			clazz = clazz.getSuperclass();
		}
		return (ParameterizedType) clazz.getGenericSuperclass();
	}

}
