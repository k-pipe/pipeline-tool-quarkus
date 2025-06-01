package org.jkube.entity;

import java.util.Optional;

public interface ResolvedEntityField<T> extends EntityField<T> {

	boolean hasValue();

	T getValue();

	Optional<T> toOptional();
}
