package pipelining.json;

public interface Object2StringMapper {
	String object2String(Object object);

	<R> R string2Object(String string, Class<R> objectClass);
}
