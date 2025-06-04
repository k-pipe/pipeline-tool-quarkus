package pipelining.pipeline;

import pipelining.logging.Log;

import java.util.Map;
import java.util.function.Consumer;

public interface PipesIn<T> {
	Map<String, Iterable<T>> getInputPipes();

	default void forAll(Consumer<T> consumer) {
		getInputPipes().values().forEach(pipe -> pipe.forEach(consumer));
	}

	default void forAllFrom(String inPipeName, Consumer<T> consumer) {
		Iterable<T> pipe = getInputPipes().get(inPipeName);
		if (pipe == null) {
			Log.warn("No such input pipe found: {}", pipe);
		} else {
			pipe.forEach(consumer);
		}
	}
}
