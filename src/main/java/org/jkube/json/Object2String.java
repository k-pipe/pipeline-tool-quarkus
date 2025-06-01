package org.jkube.json;

import static org.jkube.logging.Log.warn;

public class Object2String {

	private static Object2StringMapper OBJECT_MAPPER = null;

	public static void setMapper(Object2StringMapper mapper) {
		if ((OBJECT_MAPPER != null) && !(OBJECT_MAPPER instanceof FallbackObjectMapper)) {
			warn("ObjectMapper was set twice");
		}
		OBJECT_MAPPER = mapper;
	}

	public static String toString(Object object) {
		return getMapper().object2String(object);
	}

	public static <R> R fromString(String string, Class<R> objectClass) {
		return getMapper().string2Object(string, objectClass);
	}

	static Object2StringMapper getMapper() {
		if (OBJECT_MAPPER == null) {
			setMapper(determineObjectMapper());
		}
		return OBJECT_MAPPER;
	}

	private static Object2StringMapper determineObjectMapper() {
		return Json.isAvailable() ? Json.getMapper() : new FallbackObjectMapper();
	}

}
