package org.jkube.entity;

public enum LinkCategory {
    SET(true, false),
    LIST(false, true),
    ARRAY(false, true),
    MAP(true, true);

	private final boolean hasSource;
    private final boolean hasTarget;

    LinkCategory(boolean hasSource, boolean hasTarget) {
        this.hasSource = hasSource;
        this.hasTarget = hasTarget;
    }

    public boolean hasSource() {
        return hasSource;
    }

    public boolean hasTarget() {
        return hasTarget;
    }
}
