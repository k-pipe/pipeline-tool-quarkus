package org.jkube.job.implementation;

import org.jkube.json.Object2String;
import org.jkube.util.Utf8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

public class SetFieldOnCloseStream extends OutputStream {

	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

	private final Field field;
	private final Object obj;
	private final Class<?> valueClass;

	public SetFieldOnCloseStream(final Field field, final Object obj, Class<?> valueClass) {
		this.field = field;
		this.obj = obj;
		this.valueClass = valueClass;
	}

	@Override
	public void write(final int b) {
		baos.write(b);
	}

	@Override
	public void close() throws IOException {
		super.close();
		baos.close();
		try {
			field.setAccessible(true);
			System.out.println("DEBUG: Setting field "+field+" of type "+field.getType()+" using value class "+valueClass);
			field.set(obj, Object2String.fromString(Utf8.toString(baos.toByteArray()), valueClass));
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot acccess field "+field.getName(), e);
		}
	}

}
