package pipelining.job.implementation;

import pipelining.job.Job;
import pipelining.job.annotations.Input;
import pipelining.job.annotations.Output;
import pipelining.json.Object2String;
import pipelining.logging.Log;
import pipelining.pipeline.PipesIn;
import pipelining.pipeline.PipesOut;
import pipelining.util.Utf8;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static pipelining.logging.Log.onException;

public class JobIO {

	private final Job job;
	private final Class<?> configClass;

	public JobIO(final Job job, Class<?> configClass) {
		this.job = job;
		this.configClass = configClass;
	}

	public JobIO(final Job job) {
		this(job, null);
	}

	public List<Field> getInputs() {
		return getFields(Input.class);
	}

	public List<Field> getOutputs() {
		return getFields(Output.class);
	}

	private List<Field> getFields(final Class<? extends Annotation> annotationClass) {
		List<Field> res = new ArrayList<>();
		Class<?> ancestor = job.getClass();
		while (!ancestor.equals(Object.class)) {
			Arrays.stream(ancestor.getDeclaredFields())
					.filter(f -> f.getAnnotation(annotationClass) != null)
					.forEach(res::add);
			ancestor = ancestor.getSuperclass();
		}
		return res;
	}

	public <T> T getField(final Field field) {
		return Log.onException(() -> {
			field.setAccessible(true);
			Object inObj = field.get(job);
			if (inObj == null) {
				Log.error("Field is null: "+field.getName());
				return null;
			}
			return (T)inObj;
		}).fail("Could not get job field "+field.getName());
	}

	public <T> void setField(final Field field, T value) {
		Log.onException(() -> {
			field.setAccessible(true);
			if (field.get(job) != null) {
				Log.warn("Overwriting existing field value for "+field.getName());
			}
			field.set(job, value);
		}).fail("Could not set job field "+field.getName());
	}

	public boolean isFile(final Field field) {
		return field.getType().equals(File.class);
	}

	public boolean isOptional(final Field field) {
		return field.getType().equals(Optional.class);
	}

	public boolean isFileSystem(final Field field) {
		return field.getType().equals(FileSystem.class);
	}

	public boolean isPipesIn(final Field field) {
		return field.getType().equals(PipesIn.class);
	}

	public boolean isPipesOut(final Field field) {
		return field.getType().equals(PipesOut.class);
	}

	public InputStream getInStream(final Field field) {
		Object inObj = getField(field);
		return inObj instanceof File
				? Log.onException(() -> new FileInputStream((File) inObj)).fail("could not open file "+inObj)
				: asStream(inObj);
	}

	private InputStream asStream(final Object obj) {
		return new ByteArrayInputStream(Utf8.toBytes((Object2String.toString(obj))));
	}

	public OutputStream getOutStream(final Field field) {
		try {
			if (field.getType().equals(File.class)) {
				field.setAccessible(true);
				File file = (File) field.get(job);
				if (file == null) {
					file = new File(field.getName());
					field.set(job, file);
					Log.debug("Field of type File was null, setting to {}", field.getName());
				}
				return new FileOutputStream(file);
			} else {
				Class<?> valueClass = field.getType().equals(Object.class) && (configClass != null) ? configClass : field.getType();
				return new SetFieldOnCloseStream(field, job, valueClass);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getInputName(final Field field) {
		String value = field.getAnnotation(Input.class).value();
		return (value == null) || value.isBlank() ? field.getName() : value;
	}

	public String getOutputName(final Field field) {
		String value = field.getAnnotation(Output.class).value();
		return (value == null) || value.isBlank() ? field.getName() : value;
	}
}
