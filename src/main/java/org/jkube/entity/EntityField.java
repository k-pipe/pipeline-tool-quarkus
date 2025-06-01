package org.jkube.entity;

import java.util.function.Consumer;

public interface EntityField<T> {
	void resolve(Consumer<ResolvedEntityField<T>> consumer);
}
