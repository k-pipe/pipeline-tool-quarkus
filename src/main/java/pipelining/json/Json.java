package pipelining.json;

import pipelining.logging.Log;

import static pipelining.logging.Log.onException;

public class Json {

	private static Object2StringMapper JSON_MAPPER = null;
	private static Boolean available;

	public static void setMapper(Object2StringMapper mapper) {
		if (JSON_MAPPER != null) {
			Log.warn("Json mapper was set twice");
		}
		JSON_MAPPER = mapper;
	}

	public static String toString(Object object) {
		return getMapper().object2String(object);
	}

	public static <R> R fromString(String string, Class<R> objectClass) {
		return getMapper().string2Object(string, objectClass);
	}

	static Object2StringMapper getMapper() {
		if (JSON_MAPPER == null) {
			setMapper(determineJsonMapper());
		}
		return JSON_MAPPER;
	}

	private static Object2StringMapper determineJsonMapper() {
		return Log.onException(JacksonMapper::new).fail("Could not load json object mapper");
	}

	public static boolean isAvailable() {
		if (available == null) {
			if (JSON_MAPPER == null) {
				Log.onException(() -> setMapper(new JacksonMapper())).log("no json object mapper available");
			}
			available = JSON_MAPPER != null;
		}
		return available;
	}

}
