package org.jkube.entity;

public class LinkType {
	private LinkCategory category;
	private EntityType keyEntityType;
	private EntityType valueEntityType;

	public LinkCategory getCategory() {
		return category;
	}

	public EntityType getValueType() {
		return valueEntityType;
	}

	public EntityType getKeyType() {
		return keyEntityType;
	}

}
