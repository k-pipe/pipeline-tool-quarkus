package org.jkube.util;

import org.jkube.logging.FallbackLogger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.jkube.application.Application.fail;
import static org.jkube.logging.Log.warn;

public class Expect {

	public static Expect isTrue(boolean value) {
		return Expect.equal(true, value);
	}

	public static Expect isFalse(boolean value) {
		return Expect.equal(false, value);
	}

	public static <T> ExpectWithValue<T> present(Optional<T> optional) {
		return new ExpectWithValue(optional.orElse(null), optional.isPresent() ? null : "optional is empty");
	}

	public static <T> Expect equal(T expected, T found) {
		return new Expect(expected.equals(found) ? null : "Expected {}, found {}",expected, found);
	}

	public static Expect notNull(Object obj) {
		return new Expect(obj != null ? null : "Expected non-null object");
	}

	public static Expect isNull(Object obj) {
		return new Expect(obj == null ? null : "Expected null object");
	}

	public static Expect inInterval(int value, int min, int max) {		
		return new Expect((value >= min) && (value <= max) ? null : "value {} is not in interal {}..{}", value, min, max);
	}

	public static Expect inRange(int index, Object[] array) {
		return inInterval(index, 0, array.length-1);
	}

	public static Expect inRange(int index, List<?> list) {
		return inInterval(index, 0, list.size()-1);
	}

	public static Expect atLeast(int value, int minValue) {
		return new Expect(value >= minValue ? null : "Value {} is smaller than minimum {} ", value, minValue);
	}

	public static Expect atMost(int value, int maxValue) {
		return new Expect(value <= maxValue ? null : "Value {} is greater than maximum {}", value, maxValue);
	}

	public static Expect atLeast(long value, long minValue) {
		return new Expect(value >= minValue ? null : "Value {} is smaller than minimum {}", value, minValue);
	}

	public static Expect atMost(long value, long maxValue) {
		return new Expect(value <= maxValue ? null : "Value {} is greater than maximum {}", value, maxValue);
	}
	
	public static Expect smaller(int value, int greater) {
		return new Expect(value < greater ? null : "Value {} is not smaller than {}", value, greater);
	}

	public static Expect greater(int value, int less) {
		return new Expect(value > less ? null : "Value {} is not greater {}", value, less);
	}

	public static Expect smaller(long value, long greater) {
		return new Expect(value < greater ? null : "Value {} is not smaller {}", value, greater);
	}

	public static Expect greater(long value, long less) {
		return new Expect(value > less ? null : "Value {} is not greater {}", value, less);
	}

	public static Expect size(Collection<?> collection, int expectedSize) {
		return new Expect(collection.size() == expectedSize ? null : "Unexpected size, excpecte {}, found {} ", expectedSize, collection.size());
	}

	private final String message;
	private final Object[] parameters;

	public Expect(String message, Object... parameters) {
		this.message = message;
		this.parameters = parameters;
		if (message != null) {
			warn(message, parameters);
		}
	}

	public void elseFail() {
		if (message != null) {
			elseFail(FallbackLogger.substitute(message, parameters));
		}
	}

	public void elseFail(String failureMessage) {
		if (message != null) {
			fail(failureMessage);
		}
	}

}
