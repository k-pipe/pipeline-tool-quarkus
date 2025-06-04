package pipelining.entity;

public interface EntityData {
	
	void read(EntityDataReader dataSource);

	void read(EntityDataWriter dataTarget);

}
