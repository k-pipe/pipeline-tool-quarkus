package org.jkube.entity;

public interface EntityDataReader {
	
	long readLong();
	
	long readLong(long min, long max);
	
	double readDouble(double min, double max, double resolution);
	
	<E extends Enum<E>> E readEnum(Class<E> enumClass);
	
}
