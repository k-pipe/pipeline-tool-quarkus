package org.jkube.pipeline;

import java.util.Map;
import java.util.function.Consumer;

import static org.jkube.logging.Log.warn;

public interface PipesIn<T> {
	Map<String, Iterable<T>> getInputPipes();

	default void forAll(Consumer<T> consumer) {
		getInputPipes().values().forEach(pipe -> pipe.forEach(consumer));
	}

	default void forAllFrom(String inPipeName, Consumer<T> consumer) {
		Iterable<T> pipe = getInputPipes().get(inPipeName);
		if (pipe == null) {
			warn("No such input pipe found: {}", pipe);
		} else {
			pipe.forEach(consumer);
		}
	}
}
