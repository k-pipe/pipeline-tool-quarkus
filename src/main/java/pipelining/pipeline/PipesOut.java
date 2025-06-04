package pipelining.pipeline;

public interface PipesOut<D, E> {

	void registerOutputValue(final E pipeValue);

	void pushTo(D item, E result);

	void closeAll();
}