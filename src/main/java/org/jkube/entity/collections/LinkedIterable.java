package org.jkube.entity.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface LinkedIterable<R> extends Iterable<R> {

	int size();
	
	default boolean isEmpty() {
		return size() == 0;
	}
	
	default boolean forFirstMatching(Predicate<R> predicate, Consumer<R> action) {
		for (final R r : this) {
			if (predicate.test(r)) {
				action.accept(r);
				return true;
			}
		}
		return false;
	}

	default Optional<R> getFirstMatching(Predicate<R> predicate) {
		for (final R r : this) {
			if (predicate.test(r)) {
				return Optional.of(r);
			}
		}
		return Optional.empty();		
	}

	default <T> List<T> mapMatching(Predicate<R> predicate, Function<R, T> map) {
		List<T> res = new ArrayList<>();
		for (final R r : this) {
			if (predicate.test(r)) {
				res.add(map.apply(r));
			}
		}
		return res;			
	}

	default <T> List<T> mapAll(Function<R, T> map) {
		List<T> res = new ArrayList<>();
		for (final R r : this) {
			res.add(map.apply(r));
		}
		return res;					
	}
	
	default Stream<R> stream() {
        return StreamSupport.stream(spliterator(), false);		
	}
	
	default Stream<R> parallelStream() {
        return StreamSupport.stream(spliterator(), true);		
	}

}