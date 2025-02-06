package ubb.scs.map.guisocialnetwork.domain.entities;

public class Page <E>{
    private final Iterable<E> objects;
    private final long allObjectsCount;

    public Page( Iterable<E> objects, long allObjectsCount ) {
        this.objects = objects;
        this.allObjectsCount = allObjectsCount;
    }

    public Iterable<E> getObjects() {
        return objects;
    }

    public long getAllObjectsCount() {
        return allObjectsCount;
    }
}
