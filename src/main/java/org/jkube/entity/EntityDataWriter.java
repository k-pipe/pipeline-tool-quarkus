package org.jkube.entity;

public interface EntityDataWriter {
	
	void writeLong(long value);
	
	void writeLong(long value, long min, long max);
	
	void writeDouble(double value, double min, double max, double resolution);
	
	<E extends Enum<E>> void writeEnum(E value, Class<E> enumClass);
	
}
