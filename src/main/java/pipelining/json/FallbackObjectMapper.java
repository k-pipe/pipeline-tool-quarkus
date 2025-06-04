package pipelining.json;

import pipelining.logging.Log;

import java.io.*;
import java.util.Base64;

public class FallbackObjectMapper implements Object2StringMapper {

	private static final Base64.Decoder DECODER = Base64.getDecoder();
	private static final Base64.Encoder ENCODER = Base64.getEncoder();

	public FallbackObjectMapper() {
		Log.log("No Json object mapper found, using fallback Object2String mapping.\n"+
				"You should include the following dependency in your pom.xml to solve this:\n" +
				"   <groupId>com.fasterxml.jackson.core</groupId>\n" +
				"   <artifactId>jackson-databind</artifactId>");
	}

	@Override
	public String object2String(final Object object) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(baos)) {
			out.writeObject(object);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not serialize object of class "+object.getClass(), e);
		}
		return ENCODER.encodeToString(baos.toByteArray());
	}

	@Override
	public <R> R string2Object(final String string, final Class<R> objectClass) {
		ByteArrayInputStream bais = new ByteArrayInputStream(DECODER.decode(string));
		try (ObjectInputStream in = new ObjectInputStream(bais)) {
			return (R) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new IllegalArgumentException("Could not deserialize object of class "+objectClass, e);
		}
	}
}
