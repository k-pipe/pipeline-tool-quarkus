package pipelining.util.storage;

import pipelining.util.injection.controller.RequestContext;

import java.util.function.Consumer;

public interface DataStorageInterface {

	<D> void store(Storable<D> storable, RequestContext request, Consumer<D> proceed);

	<D> void load(Storable<D> storable, RequestContext request, Consumer<D> proceed);

	<D> void tryLoadOrInitEmpty(Storable<D> storable, RequestContext request, Consumer<D> proceed);

}