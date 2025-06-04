package pipelining.ui.domain;

public abstract class Entity {
    public abstract Long getId();
    public abstract String getName();

    @Override
    public String toString() {
        return "{id="+getId()+",name="+getName()+"}";
    }
}
