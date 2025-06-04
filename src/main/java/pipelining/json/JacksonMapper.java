package pipelining.json;

import pipelining.logging.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static pipelining.logging.Log.onException;

public class JacksonMapper implements Object2StringMapper {

	private final String CLASS = "com.fasterxml.jackson.databind.ObjectMapper";
	private final String READ_METHOD = "readValue";
	private final String WRITE_METHOD = "writeValueAsString";
	private final String SET_VISIBILITY_METHOD = "setVisibility";

	private final String PROPERTY_ENUM = "com.fasterxml.jackson.annotation.PropertyAccessor";
	private final String VISIBILITY_ENUM = "com.fasterxml.jackson.annotation.JsonAutoDetect$Visibility";

	private final String FIELD = "FIELD";
	private final String ANY = "ANY";

	private final Object mapper;
	private final Method readMethod;
	private final Method writeMethod;

	JacksonMapper()
			throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException,
			IllegalAccessException {
		final Class<?> mapperClass = Class.forName(CLASS);
		mapper = mapperClass.getDeclaredConstructor().newInstance();
		readMethod = mapperClass.getMethod(READ_METHOD, String.class, Class.class);
		writeMethod = mapperClass.getMethod(WRITE_METHOD, Object.class);
		callMethod(mapper, SET_VISIBILITY_METHOD, PROPERTY_ENUM, FIELD, VISIBILITY_ENUM, ANY);
		Log.log("Initialized Jackson ObjectMapper");
	}

	@Override
	public <R> R string2Object(final String jsonString, final Class<R> objectClass) {
		return Log.onException(() -> (R)readMethod.invoke(mapper, jsonString, objectClass)).rethrow("Could not parse Json");
	}

	@Override
	public String object2String(final Object object) {
		return Log.onException(() -> (String)writeMethod.invoke(mapper, object)).rethrow("Could not generate Json");
	}

	private void callMethod(final Object object, final String methodName,
			final String class1, final String value1, final String class2, final String value2)
			throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Class<? extends Enum> enum1 = (Class<? extends Enum>) Class.forName(class1);
		Class<? extends Enum> enum2 = (Class<? extends Enum>) Class.forName(class2);
		final Method method = object.getClass().getMethod(methodName, enum1, enum2);
		method.invoke(object, getEnumValue(enum1, value1), getEnumValue(enum2, value2));
	}

	private <E extends Enum<E>> E getEnumValue(final Class<E> enumClass, final String valueName) {
		return Enum.valueOf(enumClass, valueName);
	}

	private void resolveEnum(final String enumClass, final String valueName,
			final Map<Class<? extends Enum>, Object> enums) {
	}

}
