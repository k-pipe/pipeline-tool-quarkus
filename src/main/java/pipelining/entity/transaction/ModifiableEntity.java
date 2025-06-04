package pipelining.entity.transaction;

import pipelining.entity.Entity;

import java.util.function.Predicate;

public interface ModifiableEntity extends Entity {

	void modifyData(DataModifier<? extends ModifiableEntityData> dataModifier);

	void modifyFields(FieldsModifier<? extends ModifiableEntityFields> dataModifier);

	void link(int linkIndex, Entity linkedEntity);

	void unlink(int linkIndex);

	void expectData(Predicate<? extends ModifiableEntity> dataTester);

	void expectUnchangedFields();

	void expectChangedFields();

	void expectLinkedTo(int linkIndex, Entity linkedEntity);

	void expectLinked(int linkIndex);

	void expectUnlinked(int linkIndex);

	void expectSubclass(int subclassIndex);
}
